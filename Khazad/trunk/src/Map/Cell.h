#ifndef CELL__HEADER
#define CELL__HEADER

#include <stdafx.h>
#include <Map.h>
#include <Renderer.h>
#include <Vector3.h>
#include <bitset>

#define CELLEDGESIZE 16
#define HALFCUBE 0.5

class Building;
class Tree;

struct ROstore
{
    inline ROstore( RenderObject* n): normal(n){};
    inline ROstore(){};

    RenderObject* normal;
};

struct Face
{
    Sint16 PositiveAxisSurfaceTypeID;
    Sint16 NegativeAxisSurfaceTypeID;

    Sint16 MaterialTypeID;
};

class Cell
{

public:

    Cell();
    ~Cell();
    bool Init();
    void setPosition(CellCoordinates Coordinates);


    void Render(CameraOrientation CurrentOrientation);
    void ClearROs();

    // update VBOs
    void UpdateRenderLists(WallDisplayMode Mode);

    // sync with DF if applicable
    bool Update();

    void DrawCellCage();

    bool Draw(CubeCoordinates Coordinates);
    bool DrawFaces(CubeCoordinates Coordinates);
    bool DrawSlope(CubeCoordinates Coordinates);
    void BuildFaceData();

    Vector3 getCubePosition(CubeCoordinates Coordinates);
    MapCoordinates TranslateCubeToMap(CubeCoordinates Coordinates);
    Uint16 TranslateCubeToIndex(CubeCoordinates Coordinates);

    void setCubeShape(CubeCoordinates Coordinates, Sint16 TileShape);
    inline Sint16 getCubeShape(CubeCoordinates Coordinates)                   { return CubeShapeTypes[Coordinates.X][Coordinates.Y]; }

    void setCubeMaterial(CubeCoordinates Coordinates, Sint16 MaterialID)      { CubeMaterialTypes[Coordinates.X][Coordinates.Y] = MaterialID; }
    inline Sint16 getCubeMaterial(CubeCoordinates Coordinates)                { return CubeMaterialTypes[Coordinates.X][Coordinates.Y]; }

    void setCubeSurface(CubeCoordinates Coordinates, Sint16 SurfaceID)        { CubeSurfaceTypes[Coordinates.X][Coordinates.Y] = SurfaceID; }
    inline Sint16 getCubeSurface(CubeCoordinates Coordinates)                 { return CubeSurfaceTypes[Coordinates.X][Coordinates.Y]; }

    bool isCubeSloped(CubeCoordinates Coordinates);


    inline bool isCubeHidden(CubeCoordinates Coordinates)                         { return Hidden.test((Coordinates.X * CELLEDGESIZE) + Coordinates.Y); }
    inline void setCubeHidden(CubeCoordinates Coordinates, bool NewValue)         { Hidden.set(((Coordinates.X * CELLEDGESIZE) + Coordinates.Y), NewValue); }

    inline bool isCubeSubTerranean(CubeCoordinates Coordinates)                   { return SubTerranean.test((Coordinates.X * CELLEDGESIZE) + Coordinates.Y); }
    inline void setCubeSubTerranean(CubeCoordinates Coordinates, bool NewValue)   { SubTerranean.set(((Coordinates.X * CELLEDGESIZE) + Coordinates.Y), NewValue); }

    inline bool isCubeSkyView(CubeCoordinates Coordinates)                        { return SkyView.test((Coordinates.X * CELLEDGESIZE) + Coordinates.Y); }
    inline void setCubeSkyView(CubeCoordinates Coordinates, bool NewValue)        { SkyView.set(((Coordinates.X * CELLEDGESIZE) + Coordinates.Y), NewValue); }

    inline bool isCubeSunLit(CubeCoordinates Coordinates)                         { return SunLit.test((Coordinates.X * CELLEDGESIZE) + Coordinates.Y); }
    inline void setCubeSunLit(CubeCoordinates Coordinates, bool NewValue)         { SunLit.set(((Coordinates.X * CELLEDGESIZE) + Coordinates.Y), NewValue); }

    inline bool isCubeSolid(CubeCoordinates Coordinates)                          { return Solid.test((Coordinates.X * CELLEDGESIZE) + Coordinates.Y); }
    inline void setCubeSolid(CubeCoordinates Coordinates, bool NewValue)          { Solid.set(((Coordinates.X * CELLEDGESIZE) + Coordinates.Y), NewValue); }

    void setLiquid(CubeCoordinates Coordinates, bool liquidtype, Uint8 NewValue);


    inline Uint32 GenerateFaceKey(CubeCoordinates Coordinates, Facet FacetType);

    Face* getFace(CubeCoordinates Coordinates, Facet FacetType);
    bool hasFace(CubeCoordinates Coordinates, Facet FaceType);

    Sint16 getFaceMaterialType(CubeCoordinates Coordinates, Facet FacetType);
    bool setFaceMaterialType(CubeCoordinates Coordinates, Facet FacetType, Sint16 MaterialTypeID);

    Sint16 getFaceSurfaceType(CubeCoordinates Coordinates, Facet FacetType);
    bool setFaceSurfaceType(CubeCoordinates Coordinates, Facet FacetType, Sint16 SurfaceTypeID);

    bool removeFace(CubeCoordinates Coordinates, Facet FacetType);
    Face* addFace(CubeCoordinates Coordinates, Facet FacetType);


    void DigChannel(CubeCoordinates Coordinates);
    void DigSlope(CubeCoordinates Coordinates);
    void Dig(CubeCoordinates Coordinates);


    inline bool isActive()                              { return Active; }
    inline void setActive(bool NewValue)                { Active = NewValue; }

    inline bool isLiquidActive()                        { return ActiveLiquid; }
    inline void setLiquidActive(bool NewValue)          { ActiveLiquid = NewValue; }

    inline bool getNeedsRedraw()                        { return NeedsRedraw; }
    inline void setNeedsRedraw(bool NewValue)           { NeedsRedraw = NewValue; }

    void addBuilding(Building* NewBuilding)             { buildings.push_back(NewBuilding); }
    void addTree(Tree* NewTree)                         { trees.push_back(NewTree); }


    Vector3 getPosition()   { return CellPosition; }

    map<int16_t, vector <vertex>* > Geometry;

protected:

    bool NeedsRedraw; //TODO maintain a vector of cells that need redraw instead
    bool Active;
    bool ActiveLiquid;
    bool Initialized;

    // VBOs by texture and target
    map<int16_t, ROstore > ROs;

    Uint16 TriangleCount;

    // Data specific to each Cube
    Sint16 CubeShapeTypes[CELLEDGESIZE][CELLEDGESIZE];
    Sint16 CubeMaterialTypes[CELLEDGESIZE][CELLEDGESIZE];
    Sint16 CubeSurfaceTypes[CELLEDGESIZE][CELLEDGESIZE];

    // Bit values for each Cube
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> Hidden;
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> SubTerranean;
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> SkyView;
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> SunLit;
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> Solid;

    // Liquid Data for each Cube
    bitset<(CELLEDGESIZE * CELLEDGESIZE)> LiquidType; // Allow more liquid types?
    Uint8 LiquidLevel[CELLEDGESIZE][CELLEDGESIZE];

    // Keeps all Faces between Cubes, shares three sides with other Cubes to avoid doubling
    map<Uint32, Face*> Faces;

    vector <Building*> buildings;
    vector <Tree*> trees;

    // Exact spacial Coordinates of the center of the cell, used for frustrum culling
    Vector3 CellPosition;

    // The global position of this cell relative to other cells
    CellCoordinates thisCellCoodinates;
};

#endif // CELL__HEADER
