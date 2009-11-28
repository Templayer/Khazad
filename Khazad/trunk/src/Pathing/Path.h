#ifndef PATH__HEADER
#define PATH__HEADER

#include "Coordinates.h"
#include <vector>

struct MapPath
{
    MapPath();

    virtual MapCoordinates NextCoordinate();
    //virtual Direction NextDirection();

    virtual void ResetSteps(); // Reset to the start of the path

    float Length;   // The travel cost of the path
    int StepCount;  // The number of individual steps in the path
    int CurrentStep;  // Used to iterate the path

    int SizeLimit;      // Largest size of adjent that can use this path
    int MovementFlags;  // Booleans flags for terrain passable flags

    MapCoordinates StartCoordinates, GoalCoordinates;
};

struct FullPath: MapPath
{
    FullPath (float Cost, std::vector<MapCoordinates> Course)
    {
        Length = Cost;
        PathCourse = Course;
        StepCount = Course.size() - 1;

        StartCoordinates = PathCourse[0];
        GoalCoordinates = PathCourse[StepCount];
    }

    void ResetSteps()
    {
        CurrentStep = 0;
    }

    inline MapCoordinates NextCoordinate()
    {
        if(CurrentStep <= StepCount)
        {
            return PathCourse[CurrentStep + 1];
            CurrentStep++;
        }
        return GoalCoordinates; // Keep returning the Goal if we've reached the end of the path
    }

    std::vector<MapCoordinates> PathCourse;
};

struct VectorPath: MapPath
{
    //vector<Direction> Directions;  // Needs a definition of Directions
    std::vector<int> Magnitudes;
};

struct WayPointPath: MapPath
{
    std::vector<MapCoordinates> WayPoints;
};

#endif // PATH__HEADER