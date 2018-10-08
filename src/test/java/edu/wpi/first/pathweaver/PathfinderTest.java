package edu.wpi.first.pathweaver;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathfinderTest {

  @Test
  public void testPathfinderInstalled() {
    jaci.pathfinder.Waypoint[] points = new jaci.pathfinder.Waypoint[] {
        new jaci.pathfinder.Waypoint(-4, -1, Pathfinder.d2r(-45)),
        new jaci.pathfinder.Waypoint(-2, -2, 0),
        new jaci.pathfinder.Waypoint(0, 0, 0)
    };
    Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
        0.05, 1.7, 2.0, 60.0);

    Trajectory trajectory = Pathfinder.generate(points, config);
    assertTrue(trajectory.length() > 0);
  }
}
