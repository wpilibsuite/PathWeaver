package edu.wpi.first.pathweaver;

import javafx.scene.Group;

/**
 * Representation of a connection between points. Adds a JavaFX node that shows the connection.
 */
public interface Spline {

  void update();

  void addToGroup(Group splineGroup, double scaleFactor);

  void setEnd(Waypoint end);

  void enableSubchildSelector(int i);

  void removeFromGroup(Group splineGroup);

  void addPathWaypoint(PathDisplayController controller);
}
