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

package Nifty;

import Core.Main;
import Core.Utils;
import com.jme3.app.Application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.controls.ScrollbarChangedEvent;
import de.lessvoid.nifty.controls.Scrollbar;
import de.lessvoid.nifty.NiftyEventSubscriber;

import Game.Game;
import Game.SaveGameHeader;
import Interface.GameCameraState;
import Renderer.MapRenderer;
import Renderer.SelectionRenderer;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.xml.xpp3.Attributes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Impaler
 */
public class GameScreenController implements ScreenController, KeyInputHandler, Controller {

	private Application app;
	private Nifty nifty;
	boolean MenuUp = false;
	Element MenuPopup = null;
	Element SaveErrorPopup = null;
	Element SaveSuccessPopup = null;

	public GameScreenController(Nifty Newnifty, Application app) {
		this.app = app;
		this.nifty = Newnifty;
	}

	public void init(Properties parameter, Attributes controlDefinitionAttributes) {
	}

	public void bind(Nifty nifty, Screen screen) {
		System.out.println("bind( " + screen.getScreenId() + ")");
		screen.addKeyboardInputHandler(new KeyBoardMapping(), this);
		//screen.addPreKeyboardInputHandler(new KeyBoardMapping(), this);
	}

	public void bind(Nifty nifty, Screen screen, Element element, Properties parameter, Attributes controlDefinitionAttributes) {
	}

	public void onStartScreen() {
		System.out.println("GameScreen onStartScreen");
	}

	public void onEndScreen() {
		System.out.println("onEndScreen");
	}

	public boolean keyEvent(NiftyInputEvent event) {
		if (event != null) {
			if (event == NiftyInputEvent.Escape) {
				if (MenuUp) {
					closePopup();
					return true;
				} else {
					Menu();
					return true;
				}
			}
			//if (event == NiftyInputEvent.Activate)
		}
		return false;
	}

	public boolean inputEvent(NiftyInputEvent inputEvent) {
		return false;
	}

	public void onFocus(boolean getFocus) {
	}

	public void Menu() {
		if (MenuPopup == null) {
			MenuPopup = nifty.createPopup("MenuPopup");
		}

		Game game = app.getStateManager().getState(Game.class);
		game.Pause(true);

		nifty.showPopup(nifty.getCurrentScreen(), this.MenuPopup.getId(), null);
		MenuUp = true;
	}

	public void closePopup() {
		if (MenuPopup != null) {
			nifty.closePopup(this.MenuPopup.getId());
			MenuUp = false;
		}
	}

	public void Quit() {
		this.app.stop();
	}

	public void Abandon() {
		// Destroy Game object
		SelectionRenderer selectionRenderer = app.getStateManager().getState(SelectionRenderer.class);
		app.getStateManager().detach(selectionRenderer);
		selectionRenderer.cleanup();

		Game game = app.getStateManager().getState(Game.class);
		this.app.getStateManager().getState(MapRenderer.class).detachFromGame();
		app.getStateManager().detach(game);
		game.cleanup();

		Main core = (Main) app;
		core.getRootNode().detachAllChildren();

		closePopup();
		nifty.gotoScreen("StartScreen");
	}

	public void SaveGame() {
		// TODO maybe a GUI to pick a save game slot
		// otherwise, lets just hard code World01.sav for now

		ObjectOutputStream oos = null;

		try {
			// first, create the my documents\my games\Khazad\Worlds folder, if it does not already exist.
			JFileChooser fr = new JFileChooser();
			FileSystemView fw = fr.getFileSystemView();

			String myDocumentsFolder = fw.getDefaultDirectory().toString();
			String saveGamesFolder = myDocumentsFolder + "\\my games\\Khazad\\Worlds\\";
			File saveGamesFolderFile = new File(saveGamesFolder);
			if (!saveGamesFolderFile.exists()) {
				saveGamesFolderFile.mkdirs();
			}

			Game game = app.getStateManager().getState(Game.class);

			if (game.saveGameFileName == null) {
				// find an unused filename
				HashSet<String> saveFileNames = getFilesInFolder(saveGamesFolderFile);
				game.saveGameFileName = findUniqueFileName(saveFileNames);
			}

			// now create the save file, if it does not already exist
			File saveFile = new File(saveGamesFolder + game.saveGameFileName);
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}

			// now write to the save file
			SaveGameHeader saveGameHeader = new SaveGameHeader();
			saveGameHeader.version = game.version;
			saveGameHeader.lastPlayed = new Date(); // current time
			saveGameHeader.kingdomName = game.kingdomName;
			saveGameHeader.timeString = game.getTimeString();
			oos = new ObjectOutputStream(new FileOutputStream(saveFile));
			oos.writeObject(saveGameHeader);
			oos.writeObject(game);
			ShowSaveSuccess();
			closePopup();
		} catch (IOException e) {
			ShowSaveError(e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				ShowSaveError(e.toString());
				e.printStackTrace();
			}
		}
	}

	private HashSet<String> getFilesInFolder(File folder) {
		HashSet<String> fileNames = new HashSet<String>();
		// get all the files that end with .sav, and put in a HashSet
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				String fileEntryName = fileEntry.getName();
				if (fileEntryName.endsWith(".sav")) {
					fileNames.add(fileEntryName);
				}
			}
		}
		return fileNames;
	}

	private String findUniqueFileName(HashSet<String> saveFileNames) {
		long saveNumber = 1;
		while (true) {
			String saveNumberString = Utils.padLeadingZero(saveNumber);
			String fileName = "World" + saveNumberString + ".sav";
			if (!saveFileNames.contains(fileName)) {
				return fileName;
			}
			saveNumber++;
		}
	}

	private void ShowSaveError(String errorMessage) {
		if (SaveErrorPopup == null) {
			SaveErrorPopup = nifty.createPopup("SaveErrorPopup");
		}
		Label errorLabel = SaveErrorPopup.findNiftyControl("SaveErrorLabel", Label.class);
		if (errorLabel != null) {
			errorLabel.setText(errorMessage);
		}

		nifty.showPopup(nifty.getCurrentScreen(), this.SaveErrorPopup.getId(), null);
	}

	public void CloseSaveError() {
		if (SaveErrorPopup != null) {
			nifty.closePopup(this.SaveErrorPopup.getId());
		}
	}

	private void ShowSaveSuccess() {
		if (SaveSuccessPopup == null) {
			SaveSuccessPopup = nifty.createPopup("SaveSuccessPopup");
		}
		nifty.showPopup(nifty.getCurrentScreen(), this.SaveSuccessPopup.getId(), null);
	}

	public void CloseSaveSuccess() {
		if (SaveSuccessPopup != null) {
			nifty.closePopup(this.SaveSuccessPopup.getId());
		}
	}

	public void Pause() {
		Game game = app.getStateManager().getState(Game.class);
		game.Pause(!game.isPaused());
	}

	public void SetSpeed(String NewSpeed) {
		int speed = Integer.parseInt(NewSpeed);
		Game game = app.getStateManager().getState(Game.class);
		game.Pause(false);
		game.setTickRate(speed);
	}

	public void Dig() {
		GameCameraState Cam = app.getStateManager().getState(GameCameraState.class);
		Cam.setMode(GameCameraState.CameraMode.SELECT_VOLUME);
	}

	@NiftyEventSubscriber(id = "DepthSlider")
	public void DepthSliderChanged(final String id, final ScrollbarChangedEvent event) {

		Scrollbar bar = event.getScrollbar();
		Game game = app.getStateManager().getState(Game.class);
		int High = game.getMap().getHighestCell();
		int Low = game.getMap().getLowestCell();
		bar.setWorldMax(High - Low);

		GameCameraState camera = app.getStateManager().getState(GameCameraState.class);
		int top = camera.getSliceTop();
		int bottom = camera.getSliceBottom();

		int value = (int) event.getValue();
		int slice = camera.getSliceTop() - camera.getSliceBottom();
		camera.SetSlice(High - value, High - value - slice);
	}
}
