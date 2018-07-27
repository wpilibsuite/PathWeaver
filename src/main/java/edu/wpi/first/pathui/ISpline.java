package edu.wpi.first.pathui;

import javafx.scene.Group;

public interface ISpline {
  Path getPath();

  void update();

  void addToGroup(Group splineGroup, double scaleFactor);

  void setEnd(Waypoint end);

  void removeFromGroup(Group splineGroup);
}
