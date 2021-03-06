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

package Map;

import Data.DataManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Primary MapData storage class, holds all data for describing a 16x16 square
 * 1 level high region of the map, each tile is indexed by a byte for array and 
 * bitset based data and HashMaps hold face objects.
 * 
 * @author Impaler
 */
public class Cell implements Serializable {
	private static final long serialVersionUID = 1;

	// Larger DataValues specific to each Cube
	short[] CubeMaterialTypes;
	short[] CubeShapeTypes;

	// Bit values for each Cube
	private BitSet Hidden;
	private BitSet SubTerranean;
	private BitSet SkyView;
	private BitSet SunLit;

	// Keeps all Faces between and inside Cubes
	private HashMap<FaceCoordinate, Face> Faces;

	// The global position of this cell relative to other cells
	private CellCoordinate thisCellCoordinates;
	// Dirty values, set true on changes, set false by rendering
	transient boolean DirtyTerrainRendering;
	transient boolean DirtyPathRendering;


	public Cell() {
		CubeShape EmptyCube = new CubeShape(CubeShape.BELOW_CUBE_HEIGHT);

		CubeMaterialTypes = new short[MapCoordinate.CUBESPERCELL];
		CubeShapeTypes = new short[MapCoordinate.CUBESPERCELL];

		Hidden = new BitSet(MapCoordinate.CUBESPERCELL);
		SubTerranean = new BitSet(MapCoordinate.CUBESPERCELL);
		SkyView = new BitSet(MapCoordinate.CUBESPERCELL);
		SunLit = new BitSet(MapCoordinate.CUBESPERCELL);

		Faces = new HashMap<FaceCoordinate, Face>();

		for (int i=0; i < CubeShapeTypes.length; i++ ) {
			CubeShapeTypes[i] = EmptyCube.getData();
		}
		
		for (int i=0; i < CubeMaterialTypes.length; i++ ) {
			CubeMaterialTypes[i] = DataManager.INVALID_INDEX;
		}

		DirtyTerrainRendering = true;
		DirtyPathRendering = true;
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		// fix transients
		DirtyTerrainRendering = true;
		DirtyPathRendering = true;
	}

	public void setCellCoordinates(CellCoordinate Coordinates) {
		thisCellCoordinates = Coordinates;
		setRenderingDirty();
	}

	public CellCoordinate getCellCoordinates() {
		return thisCellCoordinates.clone(); 
	}
	
	public void setCubeShape(byte Coordinates, CubeShape NewShape) {
		if (NewShape.getData() != CubeShapeTypes[Coordinates & 0xFF]) {
			CubeShapeTypes[Coordinates& 0xFF] = NewShape.getData();

			Face TargetFace = getFace(new FaceCoordinate(Coordinates, Direction.DIRECTION_NONE));
			if (TargetFace != null) {
				setFaceShape(new FaceCoordinate(Coordinates, Direction.DIRECTION_NONE), new FaceShape(NewShape, null, Direction.DIRECTION_NONE));
			}
			setRenderingDirty();
		}
	}

	public void BuildFaceData() {
		GameMap ParentMap = GameMap.getMap();

		byte TargetCube = 0;
		do {
			CubeShape Shape = getCubeShape(TargetCube);
			short CubeMaterial = getCubeMaterial(TargetCube);

			DataManager Data = DataManager.getDataManager();
			final short WallSurface = Data.getLabelIndex("SURFACETYPE_ROUGH_WALL");
			final short FloorSurface = Data.getLabelIndex("SURFACETYPE_ROUGH_FLOOR_1");

			for (Direction DirectionType : Direction.AXIAL_DIRECTIONS) {
				FaceCoordinate FaceLocation = new FaceCoordinate(TargetCube, DirectionType);
				MapCoordinate AdjacentCoordinates = ParentMap.getFacingCoordinates(thisCellCoordinates, FaceLocation);

				if (ParentMap.isCubeInited(AdjacentCoordinates)) {
					CubeShape AdjacentShape = ParentMap.getCubeShape(AdjacentCoordinates);

					if (AdjacentShape.isSky()) {
						if (Shape.hasFace(DirectionType)) {
							Face NewFace = ParentMap.addFace(new MapCoordinate(thisCellCoordinates, TargetCube), DirectionType);

							NewFace.setFaceMaterialType(CubeMaterial);
							NewFace.setFaceSurfaceType(WallSurface);
							NewFace.setFaceShapeType(new FaceShape(Shape, AdjacentShape, DirectionType));
							if (ParentMap.isCubeSunLit(AdjacentCoordinates))
								NewFace.Sunlit = true;
						}	
					}

					if (!AdjacentShape.isEmpty()) {
						if (DirectionType == Direction.DIRECTION_DOWN && Shape.hasFloor() && AdjacentShape.hasCeiling()) {
							Face NewFace = ParentMap.addFace(new MapCoordinate(thisCellCoordinates, TargetCube), DirectionType);

							NewFace.setFaceMaterialType(ParentMap.getCubeMaterial(AdjacentCoordinates));
							NewFace.setFaceSurfaceType(FloorSurface);
							NewFace.setFaceShapeType(new FaceShape(Shape, AdjacentShape, DirectionType));
							if (ParentMap.isCubeSunLit(AdjacentCoordinates))
								NewFace.Sunlit = true;
						}
					}
				}
			}

			if (!Shape.isEmpty() && !Shape.isSolid()) {
				Face NewFace = addFace(new FaceCoordinate(TargetCube, Direction.DIRECTION_NONE));

				NewFace.setFaceMaterialType(CubeMaterial);
				NewFace.setFaceSurfaceType(FloorSurface);
				NewFace.setFaceShapeType(new FaceShape(Shape, null, Direction.DIRECTION_NONE));
				if (isCubeSunLit(TargetCube))
					NewFace.Sunlit = true;
			}
			TargetCube++;
		}
		while (TargetCube != 0);  // End Loop when Byte rolls over
		setRenderingDirty();
	}

	public void GrowGrass() {
		DataManager Data = DataManager.getDataManager();

		for (Face TargetFace : Faces.values()) {
			if (TargetFace.Sunlit) {
				 short MaterialID =  TargetFace.getFaceMaterialType();
				 int GrowthFactor = Data.getMaterialData(MaterialID).PlantGrowthFactor;
				 if (GrowthFactor > 0) {
					TargetFace.setFaceMaterialType(Data.getLabelIndex("MATERIAL_DARK_GRASS"));
				 }
			}
		}
	}

	Face getFace(FaceCoordinate TargetCoordinates) {
		return Faces.get(TargetCoordinates);
	}
	
	boolean hasFace(FaceCoordinate TargetCoordinates) { 
		return Faces.containsKey(TargetCoordinates); 
	}
	
	short getFaceMaterialType(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceMaterialType() : DataManager.INVALID_INDEX;
	}

	short getFaceSurfaceType(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceSurfaceType() : DataManager.INVALID_INDEX;
	}

	boolean setFaceMaterialType(FaceCoordinate TargetCoordinates, short MaterialTypeID) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null) {
			TargetFace.setFaceMaterialType(MaterialTypeID);
			setDirtyTerrainRendering(true);
			return true;
		}
		return false;
	}

	boolean setFaceSurfaceType(FaceCoordinate TargetCoordinates, short SurfaceTypeID) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null) {
			TargetFace.setFaceSurfaceType(SurfaceTypeID);
			setDirtyTerrainRendering(true);
			return true;
		}
		return false;
	}

	FaceShape getFaceShape(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceShapeType() : new FaceShape(new CubeShape(), new CubeShape(), Direction.DIRECTION_NONE);
	}

	boolean setFaceShape(FaceCoordinate TargetCoordinates, FaceShape NewShape) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null) {
			TargetFace.setFaceShapeType(NewShape);
			setRenderingDirty();
			return true;
		}
		return false;
	}

	boolean removeFace(FaceCoordinate TargetCoordinates) {
		if (Faces.containsKey(TargetCoordinates)) {
			Faces.remove(TargetCoordinates);
			setRenderingDirty();
			return true;
		}
		return false;
	}

	Face addFace(FaceCoordinate TargetCoordinates) {
		Face TargetFace= Faces.get(TargetCoordinates);
		if (TargetFace == null) {
			Face NewFace = new Face(TargetCoordinates.Coordinates, TargetCoordinates.FaceDirection);
			Faces.put(TargetCoordinates, NewFace);
			setRenderingDirty();
			return NewFace;
		} else {
			return TargetFace;
		}
	}
	
	public HashMap<FaceCoordinate, Face> getFaces() {
		return Faces;
	}

	public void setRenderingDirty() {
		setDirtyTerrainRendering(true);
		setDirtyPathingRendering(true);
	}

	public void setDirtyTerrainRendering(boolean dirty) {
		DirtyTerrainRendering = dirty;
	}

	public void setDirtyPathingRendering(boolean dirty) {
		DirtyPathRendering = dirty;
	}

	public boolean isTerrainRenderingDirty() {
		return DirtyTerrainRendering;
	}

	public boolean isPathingRenderingDirty() {
		return DirtyPathRendering;
	}

	public CubeShape getCubeShape(byte Coordinates)							{return new CubeShape(CubeShapeTypes[Coordinates & 0xFF]);}

	public short getCubeMaterial(byte Coordinates)							{return CubeMaterialTypes[Coordinates & 0xFF]; }
	public void setCubeMaterial(byte Coordinates, short MaterialID)			{CubeMaterialTypes[Coordinates & 0xFF] = MaterialID; DirtyTerrainRendering = true;}

	public boolean isCubeHidden(byte Coordinates)							{return Hidden.get(Coordinates & 0xFF);}
	public void setCubeHidden(byte Coordinates, boolean NewValue)			{Hidden.set(Coordinates & 0xFF, NewValue); DirtyTerrainRendering = true;}

	public boolean isCubeSubTerranean(byte Coordinates)						{ return SubTerranean.get(Coordinates & 0xFF); }
	public void setCubeSubTerranean(byte Coordinates, boolean NewValue)		{ SubTerranean.set(Coordinates & 0xFF, NewValue); DirtyTerrainRendering = true;}

	public boolean isCubeSkyView(byte Coordinates)							{ return SkyView.get(Coordinates & 0xFF); }
	public void setCubeSkyView(byte Coordinates, boolean NewValue)			{ SkyView.set(Coordinates & 0xFF, NewValue); DirtyTerrainRendering = true;}

	public boolean isCubeSunLit(byte Coordinates)							{ return SunLit.get(Coordinates & 0xFF); }
	public void setCubeSunLit(byte Coordinates, boolean NewValue)			{ SunLit.set(Coordinates & 0xFF, NewValue); DirtyTerrainRendering = true;}

	@Override
	public String toString() {
		return getClass().getName() + thisCellCoordinates.toString();
	}
}
