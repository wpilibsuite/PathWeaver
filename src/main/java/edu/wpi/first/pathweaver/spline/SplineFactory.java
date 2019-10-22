package edu.wpi.first.pathweaver.spline;

import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.path.Path;

import java.util.List;

public interface SplineFactory {
    Spline makeSpline(List<Waypoint> waypoints, Path path);
}
