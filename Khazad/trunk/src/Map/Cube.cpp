#include <stdafx.h>

#include <Cube.h>
#include <Cell.h>
#include <Map.h>
#include <Random.h>
#include <TextureManager.h>
#include <DataManager.h>
#include <ModelManager.h>

Cube::Cube()
{
    //setType(CUBE_ACTOR);

    Visible = false;
    Hidden = true;
    Initalized = false;
    data.whole = 0;

    CubeShapeType = -1;
    CubeMaterialType = -1;
    SlopeSurfaceType = -1;

    for(Facet FacetType = FACETS_START; FacetType < NUM_FACETS; ++FacetType)
    {
        FacetSurfaceTypes[FacetType] = -1;
    }
    FacetMaterialTypes[0] = -1;
    FacetMaterialTypes[1] = -1;
    FacetMaterialTypes[2] = -1;

    setPosition(0.0, 0.0, 0.0);
    MAP->ChangeCubeCount(1);
}

void Cube::SetOwner(Cell* NewOwner, Uint8 X, Uint8 Y)
{
    Owner = NewOwner;
    CellX = X % CELLEDGESIZE;
    CellY = Y % CELLEDGESIZE;

    Vector3 OwnerPosition = Owner->getPosition();

    float RealX = OwnerPosition.x - (float)(CELLEDGESIZE / 2) + (float)X + (float)HALFCUBE;
    float RealY = OwnerPosition.y - (float)(CELLEDGESIZE / 2) + (float)Y + (float)HALFCUBE;

	setPosition(RealX, RealY, OwnerPosition.z);
}

Cube::~Cube()
{
    MAP->ChangeCubeCount(-1);

    if(Initalized)
    {
        MAP->ChangeInitedCubeCount(-1);
    }
}

/*
bool Cube::DrawLiquid(float xTranslate, float yTranslate)
{
    Uint16 texture;
    Uint16 Liquid= data.liquid;
    Uint16 LiquidType= data.liquidtype;
    if(Liquid > 0)
    {
        // bind the right texture
        if(LiquidType == 1) // magma
        {
            glColor4f(1, 1, 1, 0.3);
            texture = DATA->getLabelIndex("MATERIAL_LAVA");
        }
        else
        {
            glColor4f(0.5, 0.5, 0.5, 0.7);
            texture = DATA->getLabelIndex("MATERIAL_WATER");
        }
        TEXTURE->BindTexture(texture);
        float xx= xTranslate - 0.5;
        float yy = yTranslate - 0.5;
        float z = 0.141 * Liquid - 0.5;
        float zt = 0.141 * Liquid;
        // draw quad in the appropriate height
        {
            glNormal3f(0.0,0.0,1.0);
            glTexCoord2f(0,0);
            glVertex3f(xx     ,yy    ,z);
            glTexCoord2f(0,1);
            glVertex3f(xx     ,yy + 1,z);
            glTexCoord2f(1,1);
            glVertex3f(xx + 1 ,yy + 1,z);

            glTexCoord2f(1,1);
            glVertex3f(xx + 1 ,yy + 1 ,z);
            glTexCoord2f(1,0);
            glVertex3f(xx + 1 ,yy     ,z);
            glTexCoord2f(0,0);
            glVertex3f(xx     ,yy     ,z);
        }
        Cube * north = getAdjacentCube(FACET_NORTH);
        Cube * east = getAdjacentCube(FACET_EAST);
        Cube * west = getAdjacentCube(FACET_WEST);
        Cube * south = getAdjacentCube(FACET_SOUTH);
        float nl = 0;
        if(north)
        {
            nl = north->getLiquid();
        }
        if (nl < Liquid)
        {
            float z2 =  (0.141 * nl) - 0.5;
            float zt2 =  (0.141 * nl);
            // draw a quad to patch hole north
            glTexCoord2f(0,zt2);
            glVertex3f(xx +1     ,yy    ,z2);
            glTexCoord2f(1,zt2);
            glVertex3f(xx     ,yy     ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx    ,yy    ,z);

            glTexCoord2f(0,zt2);
            glVertex3f(xx +1     ,yy    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx    ,yy    ,z);
            glTexCoord2f(0,zt);
            glVertex3f(xx +1 ,yy    ,z);

        }

        nl = 0;
        if(east)
        {
            nl = east->getLiquid();
        }
        if (nl < Liquid)
        {
            float z2 =  0.141 * nl - 0.5;
            float zt2 =  (0.141 * nl);
            // draw a quad to patch hole east
            glTexCoord2f(0,zt2);
            glVertex3f(xx +1     ,yy +1    ,z2);
            glTexCoord2f(1,zt2);
            glVertex3f(xx +1     ,yy    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx +1     ,yy     ,z);

            glTexCoord2f(0,zt2);
            glVertex3f(xx +1     ,yy +1    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx +1     ,yy     ,z);
            glTexCoord2f(0,zt);
            glVertex3f(xx +1     ,yy +1    ,z);
        }
        if(south)
        {
            nl = south->getLiquid();
        }
        if (nl < Liquid)
        {
            float z2 =  0.141 * nl - 0.5;
            float zt2 =  (0.141 * nl);
            // draw a quad to patch hole south
            glTexCoord2f(0,zt2);
            glVertex3f(xx      ,yy +1    ,z2);
            glTexCoord2f(1,zt2);
            glVertex3f(xx +1     ,yy +1    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx +1   ,yy +1     ,z);

            glTexCoord2f(0,zt2);
            glVertex3f(xx      ,yy +1    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx +1     ,yy +1     ,z);
            glTexCoord2f(0,zt);
            glVertex3f(xx      ,yy +1    ,z);
        }
        if(west)
        {
            nl = west->getLiquid();
        }
        if (nl < Liquid)
        {
            float z2 =  0.141 * nl - 0.5;
            float zt2 =  (0.141 * nl);
            // draw a quad to patch hole west
            glTexCoord2f(0,zt2);
            glVertex3f(xx      ,yy    ,z2);
            glTexCoord2f(1,zt2);
            glVertex3f(xx      ,yy +1    ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx   ,yy +1     ,z);

            glTexCoord2f(0,zt2);
            glVertex3f(xx      ,yy     ,z2);
            glTexCoord2f(1,zt);
            glVertex3f(xx      ,yy +1     ,z);
            glTexCoord2f(0,zt);
            glVertex3f(xx      ,yy     ,z);
        }
    }
}
*/
void Cube::setLiquid(Uint8 liquidtype, Uint8 NewValue)
{
    data.liquidtype = liquidtype;
    data.liquid = NewValue;

    if(NewValue > 0)
    {
        Owner->setLiquidActive(true);
    }
}

void Cube::setMaterial(Sint16 MaterialType)
{
    Initalized = true;
    CubeMaterialType = MaterialType;
}

Cube* Cube::getAdjacentCube(Facet FacetType)
{
    Sint32 x = Position.x;
    Sint32 y = Position.y;
    Sint32 z = Position.z;

    switch(FacetType)
    {
        case FACET_TOP:
            z += 1;
            break;
        case FACET_BOTTOM:
            z -= 1;
            break;
        case FACET_NORTH:
            y -= 1;
            break;
        case FACET_EAST:
            x += 1;
            break;
        case FACET_SOUTH:
            y += 1;
            break;
        case FACET_WEST:
            x -= 1;
            break;
    }

    return MAP->getCube(x, y, z);
}

Cube* Cube::getNeighborCube(Direction Type)
{
    Sint32 x = Position.x;
    Sint32 y = Position.y;
    Sint32 z = Position.z;

    switch(Type)
    {
        case DIRECTION_NORTH:
            y -= 1;
            break;
        case DIRECTION_EAST:
            x += 1;
            break;
        case DIRECTION_SOUTH:
            y += 1;
            break;
        case DIRECTION_WEST:
            x -= 1;
            break;

        case DIRECTION_NORTHEAST:
            y -= 1;
            x += 1;
            break;
        case DIRECTION_SOUTHEAST:
            y += 1;
            x += 1;
            break;
        case DIRECTION_SOUTHWEST:
            y += 1;
            x -= 1;
            break;
        case DIRECTION_NORTHWEST:
            y -= 1;
            x -= 1;
            break;

        case DIRECTION_UP:
            z += 1;
            break;
        case DIRECTION_DOWN:
            z -= 1;
            break;
    }

    return MAP->getCube(x, y, z);
}

Cell* Cube::getAdjacentCell(Facet Type)
{
    Sint32 x = Position.x;
    Sint32 y = Position.y;
    Sint32 z = Position.z;

    switch(Type)
    {
        case FACET_TOP:
            z += 1;
            break;
        case FACET_BOTTOM:
            z -= 1;
            break;
        case FACET_NORTH:
            y -= 1;
            break;
        case FACET_EAST:
            x += 1;
            break;
        case FACET_SOUTH:
            y += 1;
            break;
        case FACET_WEST:
            x -= 1;
            break;
    }

    return MAP->getCubeOwner(x, y, z);
}

bool Cube::Update()
{
	return true;
}

void Cube::Dig()
{
    /*
    setHidden(false);
    if(isSolid())
    {
        // set to floor
        setGeometry(GEOM_SLOPE);
        // reveal tiles around
        for(Direction DirectionType = DIRECTIONS_START; DirectionType < NUM_DIRECTIONS; ++DirectionType)
        {
            if(DirectionType != DIRECTION_DOWN)
            {
                Cube* NeighborCube = getNeighborCube(DirectionType);
                if(NeighborCube != NULL && NeighborCube->isHidden())
                {
                    NeighborCube->setHidden(false);
                }
            }
        }
    }
    else if(isSlope())
    {
        // set to floor
        setGeometry(GEOM_FLOOR);
        // done
        for(Direction DirectionType = DIRECTIONS_START; DirectionType < NUM_DIRECTIONS; ++DirectionType)
        {
            Cube* NeighborCube = getNeighborCube(DirectionType);
            if(NeighborCube != NULL && NeighborCube->isHidden())
            {
                NeighborCube->setHidden(false);
            }
        }
    }
    else if(isFloor())
    {
        // set empty (deleting cube would be ideal)
        setGeometry(GEOM_EMPTY);
        // reveal one tile under
        Cube * LowerCube = getNeighborCube(DIRECTION_DOWN);
        if(LowerCube)
        {
            LowerCube->setHidden(false);
        }
    }
    // update draw list of parent cell(s)
    getCellOwner()->setNeedsRedraw(true);
    for(Direction DirectionType = DIRECTIONS_START; DirectionType < NUM_DIRECTIONS; ++DirectionType)
    {
        Cube* NeighborCube = getNeighborCube(DirectionType);

        if(NeighborCube != NULL)
        {
            NeighborCube->getCellOwner()->setNeedsRedraw(true);
        }
    }
    */
}

void Cube::setFacetSurfaceType(Facet FacetType, Sint16 SurfaceType)
{
    FacetSurfaceTypes[FacetType] = SurfaceType;
}

Sint16 Cube::getFacetSurfaceType(Facet FacetType)
{
    return FacetSurfaceTypes[FacetType];
}

void Cube::setFacetMaterialType(Facet FacetType, Sint16 MaterialType)
{
    if(FacetType % 2)
    {
       FacetMaterialTypes[FacetType / 2] = MaterialType;
    }
    else
    {
        Cube* AdjacentCube = getAdjacentCube(FacetType);
        if(AdjacentCube != NULL)
        {
            AdjacentCube->setFacetMaterialType(OppositeFacet(FacetType), MaterialType);
        }
    }
}

Sint16 Cube::getFacetMaterialType(Facet FacetType)
{
    if(FacetType % 2)
    {
        return FacetMaterialTypes[FacetType / 2];
    }
    else
    {
        Cube* AdjacentCube = getAdjacentCube(FacetType);
        if(AdjacentCube != NULL)
        {
            return AdjacentCube->getFacetMaterialType(OppositeFacet(FacetType));
        }
        return -1;
    }
}

void Cube::setShape(Sint16 TileShape)
{
    CubeShapeType = TileShape;
}

bool Cube::Init()
{
    static Sint16 FloorID = DATA->getLabelIndex("TILESHAPE_FLOOR");
    static Sint16 WallID = DATA->getLabelIndex("TILESHAPE_WALL");
    static Sint16 RampID = DATA->getLabelIndex("TILESHAPE_RAMP");
    static Sint16 StairID = DATA->getLabelIndex("TILESHAPE_STAIR");
    static Sint16 FortificationID = DATA->getLabelIndex("TILESHAPE_FORTIFICATION");
    static Sint16 EmptyID = DATA->getLabelIndex("TILESHAPE_EMPTY");

    if(CubeShapeType == RampID || CubeShapeType == StairID)
    {
        setFacetMaterialType(FACET_BOTTOM, CubeMaterialType);
        Owner->setActive(true);
    }

    if(CubeShapeType == EmptyID)
    {
        if(getAdjacentCube(FACET_SOUTH) != NULL && getAdjacentCube(FACET_SOUTH)->isSolid())
        {
            setFacetMaterialType(FACET_SOUTH, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_NORTH) != NULL && getAdjacentCube(FACET_NORTH)->isSolid())
        {
            setFacetMaterialType(FACET_NORTH, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_WEST) != NULL && getAdjacentCube(FACET_WEST)->isSolid())
        {
            setFacetMaterialType(FACET_WEST, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_EAST) != NULL && getAdjacentCube(FACET_EAST)->isSolid())
        {
            setFacetMaterialType(FACET_EAST, CubeMaterialType);
        }
        Owner->setActive(true);
    }

    if(CubeShapeType == FloorID)
    {
        setFacetMaterialType(FACET_BOTTOM, CubeMaterialType);
        Owner->setActive(true);
    }

    if(isSolid())
    {
        if(getAdjacentCube(FACET_TOP) != NULL && getAdjacentCube(FACET_TOP)->isSolid())
        {
            //setFacetMaterialType(FACET_TOP, CubeMaterialType);
        }

        if(getAdjacentCube(FACET_SOUTH) != NULL && !getAdjacentCube(FACET_SOUTH)->isSolid())
        {
            setFacetMaterialType(FACET_SOUTH, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_NORTH) != NULL && !getAdjacentCube(FACET_NORTH)->isSolid())
        {
            setFacetMaterialType(FACET_NORTH, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_WEST) != NULL && !getAdjacentCube(FACET_WEST)->isSolid())
        {
            setFacetMaterialType(FACET_WEST, CubeMaterialType);
        }
        if(getAdjacentCube(FACET_EAST) != NULL && !getAdjacentCube(FACET_EAST)->isSolid())
        {
            setFacetMaterialType(FACET_EAST, CubeMaterialType);
        }

        Owner->setActive(true);
        Owner->setTopActive(true);
    }
}

bool Cube::hasFace(Facet FacetType)
{
    return getFacetMaterialType(FacetType) != -1;
}

bool Cube::Draw(float xTranslate, float yTranslate)
{
    if(isSlope())
    {
        DrawFaces(xTranslate, yTranslate);
        return DrawSlope(xTranslate, yTranslate);
    }

    return DrawFaces(xTranslate, yTranslate);
}

//TODO: pre-generating all possible configs and using them as templates would be faster. deferred
bool Cube::DrawFaces(float xTranslate, float yTranslate)
{
    // cached quads
    static const vertex vertices[6][4] =
    {
        // position, uv texture coords, normal vector - see vertex in Renderer.h
        // FACET_TOP
        vertex(-0.5f,-0.5f, 0.5f,  0.0f, 1.0f,  0.0f, 0.0f, 1.0f ),
        vertex( 0.5f,-0.5f, 0.5f,  1.0f, 1.0f,  0.0f, 0.0f, 1.0f ),
        vertex( 0.5f, 0.5f, 0.5f,  1.0f, 0.0f,  0.0f, 0.0f, 1.0f ),
        vertex(-0.5f, 0.5f, 0.5f,  0.0f, 0.0f,  0.0f, 0.0f, 1.0f ),
        // FACET_BOTTOM
        vertex(-0.5f,-0.5f,-0.5f,  0.0f, 1.0f,  0.0f, 0.0f, 1.0f ),
        vertex( 0.5f,-0.5f,-0.5f,  1.0f, 1.0f,  0.0f, 0.0f, 1.0f ),
        vertex( 0.5f, 0.5f,-0.5f,  1.0f, 0.0f,  0.0f, 0.0f, 1.0f ),
        vertex(-0.5f, 0.5f,-0.5f,  0.0f, 0.0f,  0.0f, 0.0f, 1.0f ),

        // FACET_NORTH
        vertex( 0.5f,-0.5f, 0.5f,  0.0f, 1.0f,  0.0f,-1.0f, 0.0f ),
        vertex(-0.5f,-0.5f, 0.5f,  1.0f, 1.0f,  0.0f,-1.0f, 0.0f ),
        vertex(-0.5f,-0.5f,-0.5f,  1.0f, 0.0f,  0.0f,-1.0f, 0.0f ),
        vertex( 0.5f,-0.5f,-0.5f,  0.0f, 0.0f,  0.0f,-1.0f, 0.0f ),
        // FACET_SOUTH
        vertex(-0.5f, 0.5f, 0.5f,  0.0f, 1.0f,  0.0f, 1.0f, 0.0f ),
        vertex( 0.5f, 0.5f, 0.5f,  1.0f, 1.0f,  0.0f, 1.0f, 0.0f ),
        vertex( 0.5f, 0.5f,-0.5f,  1.0f, 0.0f,  0.0f, 1.0f, 0.0f ),
        vertex(-0.5f, 0.5f,-0.5f,  0.0f, 0.0f,  0.0f, 1.0f, 0.0f ),

        // FACET_EAST
        vertex( 0.5f, 0.5f, 0.5f,  0.0f, 1.0f,  1.0f, 0.0f, 0.0f ),
        vertex( 0.5f,-0.5f, 0.5f,  1.0f, 1.0f,  1.0f, 0.0f, 0.0f ),
        vertex( 0.5f,-0.5f,-0.5f,  1.0f, 0.0f,  1.0f, 0.0f, 0.0f ),
        vertex( 0.5f, 0.5f,-0.5f,  0.0f, 0.0f,  1.0f, 0.0f, 0.0f ),
        // FACET_WEST
        vertex(-0.5f,-0.5f, 0.5f,  0.0f, 1.0f, -1.0f, 0.0f, 0.0f ),
        vertex(-0.5f, 0.5f, 0.5f,  1.0f, 1.0f, -1.0f, 0.0f, 0.0f ),
        vertex(-0.5f, 0.5f,-0.5f,  1.0f, 0.0f, -1.0f, 0.0f, 0.0f ),
        vertex(-0.5f,-0.5f,-0.5f,  0.0f, 0.0f, -1.0f, 0.0f, 0.0f )
    };

    // work vector ptr
    vector<vertex>* vec;

    if(!Visible)
    {
        return false;
    }

    for(Facet FacetType = FACETS_START; FacetType < NUM_FACETS; ++FacetType)
    {
        if(FacetType == FACET_TOP)
        {
            continue;
        }

        if(hasFace(FacetType))
        {
            Cube* c = getAdjacentCube(FacetType);
            vector< vertex >* vec;

            if(!Owner->Geometry.count(CubeMaterialType))
            {
                vec = new vector< vertex >;
                Owner->Geometry[CubeMaterialType] = vec;
            }
            else
            {
                vec = Owner->Geometry[CubeMaterialType];
            }

            vertex v3 = vertices[FacetType][3];
            v3.translate(xTranslate, yTranslate);
            vertex v2 = vertices[FacetType][2];
            v2.translate(xTranslate, yTranslate);
            vertex v1 = vertices[FacetType][1];
            v1.translate(xTranslate, yTranslate);
            vertex v0 = vertices[FacetType][0];
            v0.translate(xTranslate, yTranslate);

            vec->push_back(v3);
            vec->push_back(v1);
            vec->push_back(v0);

            vec->push_back(v3);
            vec->push_back(v2);
            vec->push_back(v1);
        }
    }
}

bool Cube::isSolid()
{
    static Sint16 WallID = DATA->getLabelIndex("TILESHAPE_WALL");
    static Sint16 FortificationID = DATA->getLabelIndex("TILESHAPE_FORTIFICATION");

   return (CubeShapeType == WallID || CubeShapeType == FortificationID);
}

bool Cube::isSlope()
{
    static Sint16 RampID = DATA->getLabelIndex("TILESHAPE_RAMP");
    static Sint16 StairID = DATA->getLabelIndex("TILESHAPE_STAIR");

   return (CubeShapeType == RampID || CubeShapeType == StairID);
}

bool Cube::DrawSlope(float xTranslate, float yTranslate)
{
    if(!Visible)
    {
        return false;
    }
    SlopeIndex surroundings;
    surroundings.value = 0;
    
    uint8_t solid;
    // copy surroundings
    for(Direction i = DIRECTION_NORTHWEST; i <= DIRECTION_WEST; ++i)
    {
        solid = 0;
        Cube *neighbor = getNeighborCube(i);
        if( neighbor != NULL)
        {
            if(neighbor->isSolid())
            {
                solid = 2;
            }
            else if (neighbor->isSlope())
            {
                solid = 1;
            }
        }
        surroundings.value |= solid << (2 * i);
    }
    
    // create output vector if needed
    // FIXME: should be part of cell?
    vector <vertex>* vec;
    if(!Owner->Geometry.count(CubeMaterialType))
    {
        vec = new vector< vertex >;
        Owner->Geometry[CubeMaterialType] = vec;
        vec->reserve(256);
    }
    else
    {
        vec = Owner->Geometry[CubeMaterialType];
    }
    // get slope geometry and mix it in
    vector <vertex> * slope = RENDERER->ModelMan->getSlope(surroundings);
    MixVertexVectorsOffset(slope, vec, xTranslate, yTranslate);
    return true;
}
