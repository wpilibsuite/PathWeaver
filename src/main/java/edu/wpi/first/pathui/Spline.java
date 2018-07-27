package edu.wpi.first.pathui;

import javafx.scene.Group;

/**
 * Representation of a connection between points. Adds a JavaFX node that shows the connection.
 */
public interface Spline {

  Path getPath();

  void update();

  void addToGroup(Group splineGroup, double scaleFactor);

  void setEnd(Waypoint end);

  void removeFromGroup(Group splineGroup);
}
