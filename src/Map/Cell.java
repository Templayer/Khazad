/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Map;

import Data.DataTypes;

import java.util.HashMap;
import java.util.BitSet;
/**
 *
 * @author Impaler
 */

public class Cell {
	
    // Larger DataValues specific to each Cube
	short[] CubeMaterialTypes;
	short[] CubeShapeTypes;

    // Bit values for each Cube
    private BitSet Hidden;
    private BitSet SubTerranean;
    private BitSet SkyView;
    private BitSet SunLit;
    private BitSet Zone;

    // Keeps all Faces between and inside Cubes
    private HashMap<FaceCoordinate, Face> Faces;
	//private HashMap<short, Face> Faces;

    // Game Objects located in the Cell for easy reference
    //List<Building> buildings;
    //List<Tree> trees;
    //List<Actor> LocalActors;

    // The global position of this cell relative to other cells
    CellCoordinate thisCellCoordinates;
	
	boolean DirtyRendering;
	
	
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
			CubeMaterialTypes[i] = DataTypes.INVALID_INDEX;
		}

		DirtyRendering = true;
	}

	public void setCellPosition(CellCoordinate Coordinates) {
		thisCellCoordinates = Coordinates;
		DirtyRendering = true;
	}

	public CellCoordinate getCellCoordinates() {
		return thisCellCoordinates; 
	}
	
	public void setCubeShape(byte Coordinates, CubeShape NewShape) {
		if (NewShape.getData() != CubeShapeTypes[Coordinates & 0xFF])
		{
			CubeShapeTypes[Coordinates& 0xFF] = NewShape.getData();
			setRenderingDirty(true);

			Face TargetFace = getFace(new FaceCoordinate(Coordinates, Direction.DIRECTION_NONE));
			if (TargetFace != null)
			{
				setFaceShape(new FaceCoordinate(Coordinates, Direction.DIRECTION_NONE), new FaceShape(NewShape, Direction.DIRECTION_NONE));
			}
		}
	}

	public void BuildFaceData() {
		GameMap ParentMap = GameMap.getMap();

		byte TargetCube = 0;
		do
		{
			CubeShape Shape = getCubeShape(TargetCube);
			short CubeMaterial = getCubeMaterial(TargetCube);

			final short NEHEMaterial = 0; //DATA->getLabelIndex("MATERIAL_UNINITIALIZED");
			final short WallSurface = 1; //DATA->getLabelIndex("SURFACETYPE_ROUGH_WALL");
			final short FloorSurface = 2; //DATA->getLabelIndex("SURFACETYPE_ROUGH_FLOOR_1");

			for (Direction DirectionType : Direction.AXIAL_DIRECTIONS)
			{
				FaceCoordinate FaceLocation = new FaceCoordinate(TargetCube, DirectionType);
				MapCoordinate ModifiedCoordinates = ParentMap.getFacingCoordinates(thisCellCoordinates, FaceLocation);
				
				CubeShape AdjacentShape = ParentMap.getCubeShape(ModifiedCoordinates);

				if (!Shape.isSky())
				{
					if (ParentMap.isCubeInited(ModifiedCoordinates) && AdjacentShape.isSky())
					{
						if (Shape.hasFace(DirectionType))
						{
							Face NewFace = ParentMap.addFace(new MapCoordinate(thisCellCoordinates, TargetCube), DirectionType);

							NewFace.setFaceMaterialType(CubeMaterial);
							NewFace.setFaceSurfaceType(WallSurface);
							NewFace.setFaceShapeType(new FaceShape(Shape, DirectionType));
						}
					}

					if (!AdjacentShape.isEmpty())
					{
						if (DirectionType == Direction.DIRECTION_DOWN && Shape.hasFloor())
						{
							Face NewFace = ParentMap.addFace(new MapCoordinate(thisCellCoordinates, TargetCube), DirectionType);

							NewFace.setFaceMaterialType(ParentMap.getCubeMaterial(ModifiedCoordinates));
							NewFace.setFaceSurfaceType(FloorSurface);
							NewFace.setFaceShapeType(new FaceShape(Shape, DirectionType));
						}
					}
				}
			}

			if (!Shape.isEmpty() && !Shape.isSolid())
			{
				Face NewFace = addFace(new FaceCoordinate(TargetCube, Direction.DIRECTION_NONE));

				NewFace.setFaceMaterialType(CubeMaterial);
				NewFace.setFaceSurfaceType(FloorSurface);
				NewFace.setFaceShapeType(new FaceShape(Shape, Direction.DIRECTION_NONE));
			}

			TargetCube++;

		}
		while (TargetCube != 0);  // End Loop when Byte rolls over
		setRenderingDirty(true);
	}

	Face getFace(FaceCoordinate TargetCoordinates) {
		return Faces.get(TargetCoordinates);
	}
	
    boolean hasFace(FaceCoordinate TargetCoordinates) { 
		return Faces.containsKey(TargetCoordinates); 
	}
	
	short getFaceMaterialType(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceMaterialType() : DataTypes.INVALID_INDEX;
	}

	short getFaceSurfaceType(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceSurfaceType() : DataTypes.INVALID_INDEX;
	}

	boolean setFaceMaterialType(FaceCoordinate TargetCoordinates, short MaterialTypeID) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null)
		{
			TargetFace.setFaceMaterialType(MaterialTypeID);
			setRenderingDirty(true);
			return true;
		}
		return false;
	}

	boolean setFaceSurfaceType(FaceCoordinate TargetCoordinates, short SurfaceTypeID) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null)
		{
			TargetFace.setFaceSurfaceType(SurfaceTypeID);
			setRenderingDirty(true);
			return true;
		}
		return false;
	}

	FaceShape getFaceShape(FaceCoordinate TargetCoordinates) {
		Face TargetFace = getFace(TargetCoordinates);
		return TargetFace != null ? TargetFace.getFaceShapeType() : new FaceShape(new CubeShape(), Direction.DIRECTION_NONE);
	}

	boolean setFaceShape(FaceCoordinate TargetCoordinates, FaceShape NewShape) {
		Face TargetFace = getFace(TargetCoordinates);

		if (TargetFace != null)
		{
			TargetFace.setFaceShapeType(NewShape);
			setRenderingDirty(true);
			return true;
		}
		return false;
	}

	boolean removeFace(FaceCoordinate TargetCoordinates) {
		if (Faces.containsKey(TargetCoordinates))
		{
			Faces.remove(TargetCoordinates);
			setRenderingDirty(true);
			return true;
		}
		return false;
	}

	Face addFace(FaceCoordinate TargetCoordinates) {
		Face TargetFace= Faces.get(TargetCoordinates);
		if (TargetFace == null) {
			Face NewFace = new Face(TargetCoordinates.Coordinates, TargetCoordinates.FaceDirection);
			Faces.put(TargetCoordinates, NewFace);
			return NewFace;
		}
		else {
			return TargetFace;
		}
	}
	
	public HashMap<FaceCoordinate, Face> getFaces() {
		return Faces;
	}

	/*
	public void addActor(Actor NewActor) {
		//Render.AddActor(NewActor);
	}

	public void removeActor(Actor DepartingActor) {
		//Render.RemoveActor(DepartingActor);
	}*/

	public void setNeedsReRendering() {
		setRenderingDirty(true);
	}

	public void setRenderingDirty(boolean dirty) {
		DirtyRendering = dirty;
	}
	
	public boolean isDirty() {
		return DirtyRendering;
	}
	
	public CubeShape getCubeShape(byte Coordinates) {
		return new CubeShape(CubeShapeTypes[Coordinates & 0xFF]); 
	}

    public void setCubeMaterial(byte Coordinates, short MaterialID) {
		CubeMaterialTypes[Coordinates & 0xFF] = MaterialID; 
	}

    public short getCubeMaterial(byte Coordinates) {
		return CubeMaterialTypes[Coordinates & 0xFF]; 
	}

    public boolean isCubeHidden(byte Coordinates) {
		return Hidden.get(Coordinates); 
	}

    public void setCubeHidden(byte Coordinates, boolean NewValue) {
		Hidden.set(Coordinates, NewValue); 
	}

	/*
    inline bool isCubeSubTerranean(CubeCoordinates Coordinates)                   { return SubTerranean.test(Coordinates); }
    inline void setCubeSubTerranean(CubeCoordinates Coordinates, bool NewValue)   { SubTerranean.set(Coordinates, NewValue); }

    inline bool isCubeSkyView(CubeCoordinates Coordinates)                        { return SkyView.test(Coordinates); }
    inline void setCubeSkyView(CubeCoordinates Coordinates, bool NewValue)        { SkyView.set(Coordinates, NewValue); }

    inline bool isCubeSunLit(CubeCoordinates Coordinates)                         { return SunLit.test(Coordinates); }
    inline void setCubeSunLit(CubeCoordinates Coordinates, bool NewValue)         { SunLit.set(Coordinates, NewValue); }

    /*
	void Cell::Save(boost::filesystem::basic_ofstream<char>& Stream) const
	{
		Stream.write((char*)&thisCellCoordinates.X, sizeof(thisCellCoordinates.X));
		Stream.write((char*)&thisCellCoordinates.Y, sizeof(thisCellCoordinates.Y));
		Stream.write((char*)&thisCellCoordinates.Z, sizeof(thisCellCoordinates.Z));

		for (int i = 0; i < CUBESPERCELL; i++)
		{
			Stream.write((char*)&CubeShapeTypes[i], sizeof(CubeShapeTypes[i]));
			Stream.write((char*)&CubeMaterialTypes[i], sizeof(CubeMaterialTypes[i]));
		}

		int16_t FaceCount = Faces.size();
		Stream.write((char*)&FaceCount, sizeof(FaceCount));
		for (std::map<uint16_t, Face*>::const_iterator it = Faces.begin(); it != Faces.end(); it++)
		{
			uint16_t Key = it->first;
			Stream.write((char*)&Key, sizeof(Key));
			it->second->Save(Stream);
		}
	}

	void Cell::Load(boost::filesystem::basic_ifstream<char>& Stream)
	{
		Stream.read((char*)&thisCellCoordinates.X, sizeof(thisCellCoordinates.X));
		Stream.read((char*)&thisCellCoordinates.Y, sizeof(thisCellCoordinates.Y));
		Stream.read((char*)&thisCellCoordinates.Z, sizeof(thisCellCoordinates.Z));

		for (int i = 0; i < CUBESPERCELL; i++)
		{
			Stream.read((char*)&CubeShapeTypes[i], sizeof(CubeShapeTypes[i]));
			Stream.read((char*)&CubeMaterialTypes[i], sizeof(CubeMaterialTypes[i]));
		}

		int16_t FaceCount;
		Stream.read((char*)&FaceCount, sizeof(FaceCount));
		for (int i = 0; i < FaceCount; i++)
		{
			uint16_t Key;
			Stream.read((char*)&Key, sizeof(Key));
			Face* NewFace = new Face();
			NewFace->Load(Stream);
			Faces[Key] = NewFace;
		}

		Render = new TerrainRendering(this);
	}
	* */
}
