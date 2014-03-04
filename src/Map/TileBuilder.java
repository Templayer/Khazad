/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Map;

import com.jme3.scene.Mesh;
//import com.jme3.util.BufferUtils;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;

import java.util.ArrayList;
import static com.jme3.util.BufferUtils.createFloatBuffer;
import static com.jme3.util.BufferUtils.createIntBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 *
 * @author Impaler
 */

public class TileBuilder {
	
	// map tileshape to mesh
	HashMap<FaceShape, Mesh> Meshes;
	
	public TileBuilder() {
		Meshes = new HashMap<FaceShape, Mesh>();
	}
	
	public Mesh getMesh(FaceShape Shape) {
		
		Mesh target = Meshes.get(Shape);

		if (target == null)
		{
			if (Shape.FaceDirection == Direction.DIRECTION_NONE)
			{
				target = CreateSlopeFace(Shape);
				Meshes.put(Shape, target);
				return target;
			} else {
				if (Shape.FaceDirection == Direction.DIRECTION_DOWN || Shape.FaceDirection == Direction.DIRECTION_UP) 
				{
					if (Shape.CubeComponent.hasFloor() || Shape.CubeComponent.hasCeiling())
					{
						target = CreateFlatFace(Shape);
						Meshes.put(Shape, target);
						return target;
					}
					return target;
				} else {
					target = CreateSideFace(Shape);
					Meshes.put(Shape, target);
					return target;
				}
			}
		}
		else
		{
			return target;
		}
	}

	public Mesh Finalize(ArrayList<Vector3f> Vertices, ArrayList<Vector3f> Normals, ArrayList<Vector2f> TextureCoords, ArrayList<Integer> Indexes) {
		
		Mesh ManualObject = new Mesh();
		
		FloatBuffer Vertbuff = createFloatBuffer(3 * Vertices.size());
        for (Vector3f element : Vertices) {
			Vertbuff.put(element.x);
			Vertbuff.put(element.y);
			Vertbuff.put(element.z);
		}
		Vertbuff.flip();
        
		FloatBuffer Normbuff = createFloatBuffer(3 * Normals.size());
        for (Vector3f element : Normals) {
			Normbuff.put(element.x);
			Normbuff.put(element.y);
			Normbuff.put(element.z);
		}
		Normbuff.flip();
		
		FloatBuffer Texbuff = createFloatBuffer(2 * TextureCoords.size());
        for (Vector2f element : TextureCoords) {
			Texbuff.put(element.x);
			Texbuff.put(element.y);
		}
		Texbuff.flip();

		IntBuffer Indxbuff = createIntBuffer(Indexes.size());
        Indxbuff.clear();
        for (Integer element : Indexes) {
			Indxbuff.put(element.intValue());
		}
        Indxbuff.flip();

		ManualObject.setBuffer(Type.Position, 3, Vertbuff);
		ManualObject.setBuffer(Type.Normal,   3, Normbuff);					
		ManualObject.setBuffer(Type.TexCoord, 2, Texbuff);
		ManualObject.setBuffer(Type.Index,    3, Indxbuff);
			
		ManualObject.updateBound();
		return ManualObject;
	}
	
	public Mesh CreateFlatFace(FaceShape Shape) {
		
		boolean Triangle1 = false;
		boolean Triangle2 = false;

		byte NorthEastCorner = Shape.CubeComponent.NorthEastCorner();
		byte NorthWestCorner = Shape.CubeComponent.NorthWestCorner();
		byte SouthEastCorner = Shape.CubeComponent.SouthEastCorner();
		byte SouthWestCorner = Shape.CubeComponent.SouthWestCorner();
				
		ArrayList<Vector3f> Vertices = new ArrayList<Vector3f>(4);
		ArrayList<Vector3f> Normals = new ArrayList<Vector3f>(4);
		ArrayList<Vector2f> TextureCoords = new ArrayList<Vector2f>(4);
		ArrayList<Integer> Indexes = new ArrayList<Integer>(4);	

		
			final int SW = 0;  final int SE = 1;  final int NW = 2;  final int NE = 3;

			Vertices.add(SW, new Vector3f(-MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, ((SouthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
			Vertices.add(SE, new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, ((SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
			Vertices.add(NW, new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, ((NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
			Vertices.add(NE, new Vector3f( MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, ((NorthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));

			Normals.add(SW, Vector3f.UNIT_Z);
			Normals.add(SE, Vector3f.UNIT_Z);
			Normals.add(NW, Vector3f.UNIT_Z);
			Normals.add(NE, Vector3f.UNIT_Z);
			
			TextureCoords.add(SW, new Vector2f(0.0f, 0.0f));
			TextureCoords.add(SE, new Vector2f(1.0f, 0.0f));
			TextureCoords.add(NW, new Vector2f(0.0f, 1.0f));
			TextureCoords.add(NE, new Vector2f(1.0f, 1.0f));
			
			if (Shape.CubeComponent.split()) // Split along NW-SE line
			{
				if ((SouthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT && NorthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT && NorthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT) || (SouthEastCorner == CubeShape.CUBE_TOP_HEIGHT && NorthEastCorner == CubeShape.CUBE_TOP_HEIGHT && NorthWestCorner == CubeShape.CUBE_TOP_HEIGHT))
				{
					Indexes.add(SE);  // South East
					Indexes.add(NE);  // North East
					Indexes.add(NW);  // North West
					
					Triangle1 = true;
				}

				if ((NorthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT && SouthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT && SouthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT) || (NorthWestCorner == CubeShape.CUBE_TOP_HEIGHT && SouthWestCorner == CubeShape.CUBE_TOP_HEIGHT && SouthEastCorner == CubeShape.CUBE_TOP_HEIGHT))
				{
					Indexes.add(NW);
					Indexes.add(SW);
					Indexes.add(SE);

					Triangle2 = true;
				}
			}
			else // Split along SW-NE line
			{
				if ((NorthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT && NorthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT && SouthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT) || (NorthEastCorner == CubeShape.CUBE_TOP_HEIGHT && NorthWestCorner == CubeShape.CUBE_TOP_HEIGHT && SouthWestCorner == CubeShape.CUBE_TOP_HEIGHT))
				{
					Indexes.add(NE);
					Indexes.add(NW);
					Indexes.add(SW);

					Triangle1 = true;
				}

				if ((SouthWestCorner == CubeShape.CUBE_BOTTOM_HEIGHT && SouthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT && NorthEastCorner == CubeShape.CUBE_BOTTOM_HEIGHT) || (SouthWestCorner == CubeShape.CUBE_TOP_HEIGHT && SouthEastCorner == CubeShape.CUBE_TOP_HEIGHT && NorthEastCorner == CubeShape.CUBE_TOP_HEIGHT))
				{
					Indexes.add(SW);
					Indexes.add(SE);
					Indexes.add(NE);

					Triangle2 = true;
				}
			}

		if (Triangle1 || Triangle2)
		{
			return Finalize(Vertices, Normals, TextureCoords, Indexes);
		} else {
			return null;
		}
	}
 
	public Mesh CreateSideFace(FaceShape Shape) {
		
		byte NorthEastCorner = Shape.CubeComponent.NorthEastCorner();
		byte NorthWestCorner = Shape.CubeComponent.NorthWestCorner();
		byte SouthEastCorner = Shape.CubeComponent.SouthEastCorner();
		byte SouthWestCorner = Shape.CubeComponent.SouthWestCorner();		
		
				
		if (NorthWestCorner == CubeShape.CUBE_TOP_HEIGHT && Shape.FaceDirection == Direction.DIRECTION_WEST) {
			int de = 0;
			int bug = de;
		}

		ArrayList<Vector3f> Vertices = new ArrayList<Vector3f>();
		ArrayList<Vector3f> Normals = new ArrayList<Vector3f>();
		ArrayList<Vector2f> TextureCoords = new ArrayList<Vector2f>();
		ArrayList<Integer> Indexes = new ArrayList<Integer>();	

		boolean Triangle = false;
		CubeShape Cube = Shape.CubeComponent;

		
		float XLeft = 0; float XRight = 0; float YLeft = 0; float YRight = 0;
		int LeftCorner = 0; int RightCorner = 0;
		Vector3f Normal = new Vector3f();

		switch (Shape.FaceDirection)
		{
			case DIRECTION_SOUTH:
				XLeft = -MapCoordinate.HALFCUBE;  YLeft = -MapCoordinate.HALFCUBE;  XRight =  MapCoordinate.HALFCUBE;  YRight = -MapCoordinate.HALFCUBE;
				LeftCorner = Cube.SouthWestCorner();   RightCorner = Cube.SouthEastCorner();
				Normal = Vector3f.UNIT_Y.negate();
				break;

			case DIRECTION_NORTH:
				XLeft =  MapCoordinate.HALFCUBE;  YLeft =  MapCoordinate.HALFCUBE;  XRight = -MapCoordinate.HALFCUBE;  YRight =  MapCoordinate.HALFCUBE;
				LeftCorner = Cube.NorthEastCorner();  RightCorner = Cube.NorthWestCorner();
				Normal = Vector3f.UNIT_Y;
				break;

			case DIRECTION_WEST:
				XLeft = -MapCoordinate.HALFCUBE;  YLeft =  MapCoordinate.HALFCUBE;  XRight = -MapCoordinate.HALFCUBE;  YRight = -MapCoordinate.HALFCUBE;
				LeftCorner = Cube.NorthWestCorner();  RightCorner = Cube.SouthWestCorner();
				Normal = Vector3f.UNIT_X.negate();
				break;

			case DIRECTION_EAST:
				XLeft =  MapCoordinate.HALFCUBE;  YLeft = -MapCoordinate.HALFCUBE;  XRight =  MapCoordinate.HALFCUBE;  YRight =  MapCoordinate.HALFCUBE;
				LeftCorner = Cube.SouthEastCorner();  RightCorner = Cube.NorthEastCorner();
				Normal = Vector3f.UNIT_X;
				break;

			default:
				break;
		}

		float Left = (Math.min(Math.max(LeftCorner, CubeShape.CUBE_BOTTOM_HEIGHT), CubeShape.CUBE_TOP_HEIGHT) - 1.0f) / CubeShape.HEIGHT_FRACTIONS;
		float Right = (Math.min(Math.max(RightCorner, CubeShape.CUBE_BOTTOM_HEIGHT), CubeShape.CUBE_TOP_HEIGHT) - 1.0f) / CubeShape.HEIGHT_FRACTIONS;

		Vertices.add(new Vector3f(XLeft,  YLeft, -MapCoordinate.HALFCUBE));  // Left Bottom
		Normals.add(Normal);
		TextureCoords.add(new Vector2f(0.0f, 0.0f));

		Vertices.add(new Vector3f(XLeft, YLeft, Left - MapCoordinate.HALFCUBE));  // Left Top
		Normals.add(Normal);
		TextureCoords.add(new Vector2f(0.0f, Left));

		Vertices.add(new Vector3f(XRight, YRight, -MapCoordinate.HALFCUBE));  // Right Bottom
		Normals.add(Normal);
		TextureCoords.add(new Vector2f(1.0f, 0.0f));

		Vertices.add(new Vector3f(XRight, YRight, Right - MapCoordinate.HALFCUBE));  // Right Top
		Normals.add(Normal);
		TextureCoords.add(new Vector2f(1.0f, Right));

		if (LeftCorner != CubeShape.BELOW_CUBE_HEIGHT && RightCorner != CubeShape.BELOW_CUBE_HEIGHT)
		{
			Indexes.add(3);
			Indexes.add(1);
			Indexes.add(0);
			Triangle = true;
		}

		if (LeftCorner != CubeShape.BELOW_CUBE_HEIGHT && RightCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
		{
			Indexes.add(0);
			Indexes.add(2);
			Indexes.add(3);
			Triangle = true;
		}
		

		if (Triangle) {
			return Finalize(Vertices, Normals, TextureCoords, Indexes);
		} else {
			return null;
		}
	}

	public Mesh CreateSlopeFace(FaceShape Shape) {
		
		boolean Triangle1 = false;
		boolean Triangle2 = false;

		byte NorthEastCorner = Shape.CubeComponent.NorthEastCorner();
		byte NorthWestCorner = Shape.CubeComponent.NorthWestCorner();
		byte SouthEastCorner = Shape.CubeComponent.SouthEastCorner();
		byte SouthWestCorner = Shape.CubeComponent.SouthWestCorner();

		ArrayList<Vector3f> Vertices = new ArrayList<Vector3f>();
		ArrayList<Vector3f> Normals = new ArrayList<Vector3f>();
		ArrayList<Vector2f> TextureCoords = new ArrayList<Vector2f>();
		ArrayList<Integer> Indexes = new ArrayList<Integer>();	

		final int SW = 0;  final int SE = 1;  final int NW = 2;  final int NE = 3;

		{
			Vector3f SWv = new Vector3f(-MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE);
			Vector3f SEv = new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE);
			Vector3f NWv = new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE);
			Vector3f NEv = new Vector3f( MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE);

			Vector3f NWBv = new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE);
			Vector3f SEBv = new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE);
			Vector3f NEBv = new Vector3f( MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE);
			Vector3f SWBv = new Vector3f(-MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE);

					
			Vector3f Normal = new Vector3f();
			
			Vector3f X = Vector3f.UNIT_X;
			Vector3f Y = Vector3f.UNIT_Y;

			if (Shape.CubeComponent.split()) // Split along the NW-SE line
			{
				// Triangle1 SE->NE->NW
				if (SouthEastCorner > CubeShape.BELOW_CUBE_HEIGHT && NorthEastCorner > CubeShape.BELOW_CUBE_HEIGHT && NorthWestCorner > CubeShape.BELOW_CUBE_HEIGHT)
				{
					if (SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT || NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT || NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						if (SouthEastCorner < CubeShape.CUBE_TOP_HEIGHT || NorthEastCorner < CubeShape.CUBE_TOP_HEIGHT || NorthWestCorner < CubeShape.CUBE_TOP_HEIGHT)
						{
							Vertices.add(SEv);
							Normals.add(new Vector3f( NEv.subtract(SEv).cross( NWv.subtract(SEv)).normalize()));
							TextureCoords.add(new Vector2f(1.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(NEv);
							Normals.add(new Vector3f( NWv.subtract(NEv)).cross( SEv.subtract(NEv) ).normalize());
							TextureCoords.add(new Vector2f(1.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(NWv);
							Normals.add(new Vector3f( SEv.subtract(NWv)).cross( NEv.subtract(NWv) ).normalize());
							TextureCoords.add(new Vector2f(0.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);

							Triangle1 = true;
						}
					}
				}

				// Triangle2 NW->SW->SE
				if (NorthWestCorner > CubeShape.BELOW_CUBE_HEIGHT && SouthWestCorner > CubeShape.BELOW_CUBE_HEIGHT && SouthEastCorner > CubeShape.BELOW_CUBE_HEIGHT)
				{
					if (NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						if (NorthWestCorner < CubeShape.CUBE_TOP_HEIGHT || SouthWestCorner < CubeShape.CUBE_TOP_HEIGHT || SouthEastCorner < CubeShape.CUBE_TOP_HEIGHT)
						{
							Vertices.add(new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
							Normals.add(new Vector3f( SWv.subtract(NWv).cross(SEv.subtract(NWv)).normalize()));
							TextureCoords.add(new Vector2f(0.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(new Vector3f(-MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
							Normals.add(new Vector3f( SEv.subtract(SWv)).cross(NWv.subtract(SWv)).normalize());
							TextureCoords.add(new Vector2f(0.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
							Normals.add(new Vector3f( NWv.subtract(SEv)).cross(SWv.subtract(SEv)).normalize());
							TextureCoords.add(new Vector2f(1.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);

							Triangle2 = true;
						}
					}
				}

				// Vertical face inside Cube when only one triangle is drawn
				if ((Triangle1 ^ Triangle2) && ((NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)))
				{
					if ((Triangle1 && SouthWestCorner < CubeShape.CUBE_TOP_HEIGHT) || (Triangle2 && NorthEastCorner < CubeShape.CUBE_TOP_HEIGHT))
					{
						/*
						Vector3f Vertex4 = new Vector3f( -MapCoordinates.HALFCUBE, MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE);
						Vector3f Vertex5 = new Vector3f( MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE);

						
						Vertices.add(new Vector3f(-MapCoordinates.HALFCUBE,  MapCoordinates.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinates.HALFCUBE));
						Normals.add(Normal);
						TextureCoords.add(new Vector2f(1.0f - ((float) NorthWestCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));

						Vertices.add(new Vector3f( MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinates.HALFCUBE));
						Normals.add(Normal);
						TextureCoords.add(new Vector2f(1.0f - ((float) SouthEastCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));

						Vertices.add(new Vector3f( -MapCoordinates.HALFCUBE, MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE));  // North West Bottom  5
						Normals.add(Normal);
						TextureCoords.add(new Vector2f(1.0f, 0.0f));

						Vertices.add(new Vector3f( MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE, -MapCoordinates.HALFCUBE));  // South East Bottom  6
						Normals.add(Normal);
						TextureCoords.add(new Vector2f(1.0f, 1.0f));
*/

						if (Triangle1)
						{
							Normal = X.negate().add(Y.negate());

							if (NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthEastCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthWestCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(new Vector3f( -MapCoordinate.HALFCUBE, MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // North West Bottom  5
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);
								
								// SE->NW->NW Bottom
							}
							if (SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(new Vector3f( -MapCoordinate.HALFCUBE, MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // North West Bottom  5
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // South East Bottom  6
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthEastCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								// NW Bottom->SE Bottom->SE
							}
						}
						
						if (Triangle2)
						{
							Normal = X.add(Y);

							if (SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthWestCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, (((float) SouthEastCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthEastCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // South East Bottom  6
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);

								//ManualObject->triangle(3, 4, 6);  // NW->SE->SE Bottom
							}
							if (NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(new Vector3f( MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // South East Bottom  6
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(new Vector3f( -MapCoordinate.HALFCUBE, MapCoordinate.HALFCUBE, -MapCoordinate.HALFCUBE));  // North West Bottom  5
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(new Vector3f(-MapCoordinate.HALFCUBE,  MapCoordinate.HALFCUBE, (((float) NorthWestCorner - 1) / CubeShape.HEIGHT_FRACTIONS) -MapCoordinate.HALFCUBE));
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthWestCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);

								//ManualObject->triangle(6, 5, 3);  // SE Bottom->NW Bottom->NW
							}
						}
					}
				}

				// Vertical bisector through whole cube
				if (!Triangle1 && !Triangle2 && NorthWestCorner >= CubeShape.CUBE_TOP_HEIGHT && SouthEastCorner >= CubeShape.CUBE_TOP_HEIGHT)
				{
					if (NorthEastCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						Normal = X.add(Y);
					}
					if (SouthWestCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						Normal = X.negate().add(Y.negate());	
					}

					int Start = Vertices.size();
					
					Vertices.add(NWv);  // North West  0
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(0.0f, 0.0f));

					Vertices.add(SEv);  // South East  1
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(0.0f, 1.0f));

					Vertices.add(NWBv);  // North West Bottom  2
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(1.0f, 0.0f));

					Vertices.add(SEBv);  // South East Bottom  3
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(1.0f, 1.0f));

					if (NorthEastCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						// NW Bottom-> NW-> SE
						Indexes.add(Start + 2);
						Indexes.add(Start + 0);
						Indexes.add(Start + 1); 
						
						// SE ->SE Bottom-> NW Bottom
						Indexes.add(Start + 1);
						Indexes.add(Start + 3);
						Indexes.add(Start + 2);
					}
					else
					{
						// SE-> NW-> NW Bottom
						Indexes.add(Start + 1);
						Indexes.add(Start + 0);
						Indexes.add(Start + 2);

						// NW Bottom ->SE Bottom-> SE
						Indexes.add(Start + 2);
						Indexes.add(Start + 3);
						Indexes.add(Start + 1); 
					}

					Triangle1 = true;
					Triangle2 = true;
				 }
			}
			else // Split along the SW-NE line
			{
				// Triangle1 NE->NW->SW
				if (NorthEastCorner > CubeShape.BELOW_CUBE_HEIGHT && NorthWestCorner > CubeShape.BELOW_CUBE_HEIGHT && SouthWestCorner > CubeShape.BELOW_CUBE_HEIGHT)
				{
					if (NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT || NorthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						if (NorthEastCorner < CubeShape.CUBE_TOP_HEIGHT || NorthWestCorner < CubeShape.CUBE_TOP_HEIGHT || SouthWestCorner < CubeShape.CUBE_TOP_HEIGHT)
						{
							Vertices.add(NEv);  // North East
							Normals.add( (NWv.subtract(NEv)).cross(SWv.subtract(NEv)).normalize());
							TextureCoords.add(new Vector2f(1.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);
							
							Vertices.add(NWv);  // North West
							Normals.add( (SWv.subtract(NWv)).cross( NEv.subtract(NWv)).normalize());
							TextureCoords.add(new Vector2f(0.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);
							
							Vertices.add(SWv);  // South West
							Normals.add( (NEv.subtract(SWv)).cross( NWv.subtract(SWv)).normalize());
							TextureCoords.add(new Vector2f(0.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);
							
							Triangle1 = true;
						}
					}
				}

				// Triangle2 SW->SE->NE
				if (SouthWestCorner > CubeShape.BELOW_CUBE_HEIGHT && SouthEastCorner > CubeShape.BELOW_CUBE_HEIGHT && NorthEastCorner > CubeShape.BELOW_CUBE_HEIGHT)
				{
					if (SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT || NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						if (SouthWestCorner < CubeShape.CUBE_TOP_HEIGHT || SouthEastCorner < CubeShape.CUBE_TOP_HEIGHT || NorthEastCorner < CubeShape.CUBE_TOP_HEIGHT)
						{
							Vertices.add(SWv);  // South West
							Normals.add(( SEv.subtract(SWv)).cross( NEv.subtract(SWv)).normalize());
							TextureCoords.add(new Vector2f(0.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(SEv);  // South East
							Normals.add(( NEv.subtract(SEv)).cross( SWv.subtract(SEv)).normalize());
							TextureCoords.add(new Vector2f(1.0f, 0.0f));
							Indexes.add(Vertices.size() - 1);

							Vertices.add(NEv);  // North East
							Normals.add(( SWv.subtract(NEv)).cross( SEv.subtract(NEv)).normalize());
							TextureCoords.add(new Vector2f(1.0f, 1.0f));
							Indexes.add(Vertices.size() - 1);
							
							Triangle2 = true;
						}
					}
				}

				// Vertical face inside Cube when only one triangle is drawn
				if ((Triangle1 ^ Triangle2) && ((NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT || SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)))
				{
					if ((Triangle1 && SouthEastCorner < CubeShape.CUBE_TOP_HEIGHT) || (Triangle2 && NorthWestCorner < CubeShape.CUBE_TOP_HEIGHT))
					{
						if (Triangle1)
						{
							Normal = X.add(Y.negate());

							if (NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(SWBv);  // South West Bottom  6
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(NEBv);  // North East Bottom  5
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(NEv);  // North East  3
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthEastCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);
								
								// SW Bottom->NE Bottom->NE
							}
							if (SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(NEv);  // North East
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthEastCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(SWv);  // South West
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthWestCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(SWBv);  // South West Bottom
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);

								// NE->SW->SW Bottom
							}
						}
						if (Triangle2)
						{
							Normal = Y.add(X.negate());
		
							if (NorthEastCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(SWv);  // South West  4
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthWestCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);
								
								Vertices.add(NEv);  // North East  3
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) NorthEastCorner / CubeShape.HEIGHT_FRACTIONS), 0.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(NEBv);  // North East Bottom  5
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);

								// SW->NE->NE Bottom
							}
							if (SouthWestCorner > CubeShape.CUBE_BOTTOM_HEIGHT)
							{
								Vertices.add(NEBv);  // North East Bottom
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 0.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(SWBv);  // South West Bottom
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f, 1.0f));
								Indexes.add(Vertices.size() - 1);

								Vertices.add(SWv);  // South West
								Normals.add(Normal);
								TextureCoords.add(new Vector2f(1.0f - ((float) SouthWestCorner / CubeShape.HEIGHT_FRACTIONS), 1.0f));
								Indexes.add(Vertices.size() - 1);
							
								// NE Bottom->SW Bottom->SW
							}
						}
					}
				}

				// Vertical bisector through whole cube
				if (!Triangle1 && !Triangle2 && SouthWestCorner >= CubeShape.CUBE_TOP_HEIGHT && NorthEastCorner >= CubeShape.CUBE_TOP_HEIGHT)
				{
					if (SouthEastCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						Normal = (X.negate()).add(Y.negate());
					}
					if (NorthWestCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						Normal = X.add(Y);
					}

					int Start = Vertices.size();
							
					Vertices.add(NEv);  // North East
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(0.0f, 0.0f));

					Vertices.add(SWv);  // South West
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(0.0f, 1.0f));

					Vertices.add(NEBv);  // North East Bottom
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(1.0f, 0.0f));

					Vertices.add(SWBv);  // South West Bottom
					Normals.add(Normal);
					TextureCoords.add(new Vector2f(1.0f, 1.0f));

					if (SouthEastCorner < CubeShape.CUBE_BOTTOM_HEIGHT)
					{
						Indexes.add(Start + 2);
						Indexes.add(Start + 0);
						Indexes.add(Start + 1);
						// NW Bottom-> NW-> SW
						
						Indexes.add(Start + 1);
						Indexes.add(Start + 3);
						Indexes.add(Start + 2);
						// SW ->SW Bottom-> NW Bottom
					}
					else
					{
						Indexes.add(Start + 1);
						Indexes.add(Start + 0);
						Indexes.add(Start + 2);
						 // SE-> NW-> NW Bottom
						
						Indexes.add(Start + 2);
						Indexes.add(Start + 3);
						Indexes.add(Start + 1);
						// NW Bottom ->SE Bottom-> SE
					}

					Triangle1 = true;
					Triangle2 = true;
				 }
			}
		}

		if (Triangle1 || Triangle2) {
			return Finalize(Vertices, Normals, TextureCoords, Indexes);
		} else {
			return null;
		}
	}
}

