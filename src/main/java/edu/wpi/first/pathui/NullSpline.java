package edu.wpi.first.pathui;

import javafx.scene.Group;

public class NullSpline implements ISpline {
  private final Path path;

  public NullSpline(Path path) {
    this.path = path;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public void update() {
  }

  @Override
  public void addToGroup(Group splineGroup, double scaleFactor) {

  }

  @Override
  public void setEnd(Waypoint end) {

  }

  @Override
  public void removeFromGroup(Group splineGroup) {

  }
}
