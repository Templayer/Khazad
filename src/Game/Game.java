/* Copyright 2010 Kenneth 'Impaler' Ferland

This file is part of Khazad.

Khazad is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Khazad is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Khazad.  If not, see <http://www.gnu.org/licenses/> */

package Game;

import Core.Main;
import Core.Dice;
import Core.Utils;
import Job.ExcavateJob;
import Job.JobManager;
import Map.*;
import Terrain.Geology;
import Interface.VolumeSelection;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.PriorityQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Game holds all the objects (Map, Settlment, Weather etc) which together make
 * up the simulation and is were saving and loading of game files is initiate
 * before being piped to the particular sub-objects. Game is also responsible
 * for the simulation logic loop which is called by update each frame, a Ticker
 * instance is created and submitted to the thread pool to keep the core JME
 * thread fully devoted to rendering.
 *
 * @author Impaler
 */
public class Game extends AbstractAppState implements ActionListener, Serializable {
	private static final long serialVersionUID = 1;

	public static String version = "0.2.2";
	transient SimpleApplication app = null;
	transient AppStateManager state = null;
	int MasterSeed;
	Dice PawnDice = new Dice();
	GameMap MainMap;
	Geology MapGeology;
	Settlement GameSettlement;
	Weather GameWeather;
	boolean Pause;
	int TickRate;
	long CurrentGameTick;
	float TickRounding;
	public long seconds;
	public long minutes;
	public long hours;
	public long days;
	int UniqueIDCounter;
	PriorityQueue<Temporal> TemporalQueue;
	ArrayList<Actor> Actors;
	int ActorIDcounter = 0;
	transient ExecutorService Executor;
	transient Future lastUpdate;
	public transient String kingdomName;
	public transient String saveGameFileName;

	public Game() {

		TickRate = 1;
		TickRounding = 0;
		CurrentGameTick = Temporal.TICKS_PER_DAY / 2;
		UniqueIDCounter = 0;
		Pause = true;

		TemporalQueue = new PriorityQueue<Temporal>();
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		this.app = (SimpleApplication) app;
		this.state = stateManager;
		Main core = (Main) app;
		Executor = core.getThreadPool();

		registerWithInput(app.getInputManager());
	}


	public boolean InitializeGame(short X, short Y, String SeedString, String kingdomName) {
		MasterSeed = SeedString.hashCode();
		this.kingdomName = kingdomName;
		saveGameFileName = null;
		PawnDice.Seed(MasterSeed);

		MapGeology = new Geology();
		MapGeology.Initialize(MasterSeed);
		MapGeology.GenerateWorldHeightMap(X, Y);

		MainMap = new GameMap();
		MainMap.Initialize(MasterSeed);

		GameWeather = new Weather();
		AddTemporal(GameWeather);

		BuildMapChunk((short) 0, (short) 0, (byte) X, (byte) Y);

		GameSettlement = new Settlement();
		Actors = new ArrayList<Actor>();

		return true;
	}

	boolean BuildMapChunk(short X, short Y, byte Width, byte Height) {
		short SizeX = (short) (X + Width);
		short SizeY = (short) (Y + Height);

		// Create and add Cells with shape and material data
		for (int x = X; x < SizeX; x++) {
			for (int y = Y; y < SizeY; y++) {
				MapGeology.GenerateCellHeight(x, y, (float) 10.0, (float) 1.0);

				for (int z = MapGeology.getCellBottomZLevel() - 2; z <= MapGeology.getCellTopZLevel() + 2; z++) {
					CellCoordinate TargetCellCoordinates = new CellCoordinate(x, y, z);
					Cell NewCell = new Cell();

					NewCell.setCellCoordinates(TargetCellCoordinates);
					MainMap.insertCell(NewCell);
					MapGeology.LoadCellData(NewCell);
				}
			}
		}

		MainMap.GenerateFirstLight();

		for (Cell TargetCell : MainMap.getCellCollection()) {
			TargetCell.BuildFaceData();
			TargetCell.GrowGrass();
		}

		return true;
	}

	public Pawn SpawnPawn(MapCoordinate SpawnCoordinates, short CreatureTypeID) {
		Pawn NewPawn = new Pawn(CreatureTypeID, ActorIDcounter, MasterSeed, SpawnCoordinates);
		ActorIDcounter++;
		Actors.add(NewPawn);
		AddTemporal(NewPawn);
		return NewPawn;
	}

	public Citizen SpawnCitizen(short CreatureTypeID, MapCoordinate SpawnCoordinates) {
		Citizen NewCitizen = new Citizen(CreatureTypeID, ActorIDcounter, PawnDice.Roll(0, MasterSeed), SpawnCoordinates);
		ActorIDcounter++;
		Actors.add(NewCitizen);
		GameSettlement.addCitizen(NewCitizen);
		AddTemporal(NewCitizen);
		return NewCitizen;
	}

	boolean AddTemporal(Temporal NewTemporal) {
		TemporalQueue.add(NewTemporal);
		return true;
	}

	public GameMap getMap() {
		return MainMap;
	}

	public Settlement getSettlement() {
		return GameSettlement;
	}

	public Weather getWeather() {
		return GameWeather;
	}

	public void VolumeSelectionCompleted(VolumeSelection newVolume) {
		// What dose it mean?  need some kind of priming knowlege

		JobManager jobs = GameSettlement.getJobManager();
		ExcavateJob newJob = new ExcavateJob(MainMap);
		ArrayList Volumes = new ArrayList<VolumeSelection>();
		Volumes.add(newVolume);
		Zone newZone = getMap().createZone(Volumes);

		newJob.addDesignations(newVolume, newZone, new CubeShape(CubeShape.CUBE_BOTTOM_HEIGHT));
		jobs.addJob(newJob);
	}

	public ArrayList<Actor> getActors() {
		return Actors;
	}

	public void onAction(String name, boolean keyPressed, float tpf) {
		if (this.isEnabled()) {
			if (name.equals("Pause")) {
				if (keyPressed)
					Pause = !Pause;
			}
			if (name.equals("Faster")) {
				if (keyPressed)
					TickRate *= 4;
			}
			if (name.equals("Slower")) {
				if (keyPressed)
					TickRate /= 4;
			}
		}
	}

	public void registerWithInput(InputManager inputManager) {
		String[] inputs = {"Pause", "Faster", "Slower"};

		inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Faster", new KeyTrigger(KeyInput.KEY_ADD));
		inputManager.addMapping("Faster", new KeyTrigger(KeyInput.KEY_EQUALS));
		inputManager.addMapping("Slower", new KeyTrigger(KeyInput.KEY_MINUS));
		inputManager.addMapping("Slower", new KeyTrigger(KeyInput.KEY_SUBTRACT));
		inputManager.addListener(this, inputs);
	}
	
	@Override
	public void update(float tpf) {
		if (!Pause) {
			if (lastUpdate == null || lastUpdate.isDone()) {
				float TargetTicks = TickRate * tpf * Temporal.TICKS_PER_SECOND;
				TargetTicks += TickRounding;
				int FullTicks = (int) TargetTicks;
				TickRounding = TargetTicks - FullTicks;

				Ticker simulation = new Ticker(this);
				simulation.windup(FullTicks);
				lastUpdate = Executor.submit(simulation);

				seconds = CurrentGameTick / Temporal.TICKS_PER_SECOND;
				minutes = CurrentGameTick / Temporal.TICKS_PER_MINUTE;
				hours = CurrentGameTick / Temporal.TICKS_PER_HOUR;
				days = CurrentGameTick / Temporal.TICKS_PER_DAY;
			}
		}
	}

	public long getCurrentTimeTick() {
		return CurrentGameTick;
	}

	public void setTickRate(int NewRate) {
		TickRate = NewRate;
	}

	public int getTickRate() {
		return TickRate;
	}

	public void Pause(boolean NewPause) {
		Pause = NewPause;
	}

	public boolean isPaused() {
		return Pause;
	}

	// this method is used by serialization
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		// fix transients
		app = Main.app;
		state = Main.app.getStateManager();
		Executor = Main.app.getThreadPool();
		lastUpdate = null;
	}
	
	public String getTimeString() {
		String hoursString = Utils.padLeadingZero(hours %25);
		String minutesString = Utils.padLeadingZero(minutes %60);
		String secondsString = Utils.padLeadingZero(seconds %60);
		return "DAY " + days + "  -  " + hoursString + ":" + minutesString + ":" + secondsString;
	}
}
