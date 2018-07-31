package edu.wpi.first.pathweaver;

import javafx.scene.Group;

/**
 * Representation of a connection between points. Adds a JavaFX node that shows the connection.
 */
public interface Spline {
  /**
   * Makes a spline.
   *
   * @param first Waypoint at start of spline
   * @param last  Waypoing at end of spline
   */
  public Spline(Waypoint first, Waypoint last) {
    start = first;
    end = last;
    cubic = new CubicCurve();
    cubic.getStyleClass().add("path");
    FxUtils.applySubchildClasses(cubic);
    start.addSpline(this, true);
    end.addSpline(this, false);
    updateControlPoints();

  void update();

  void addToGroup(Group splineGroup, double scaleFactor);

  void setEnd(Waypoint end);
  void enableSubchildSelector(int i) {
    FxUtils.enableSubchildSelector(cubic, i);
    cubic.applyCss();
  }

  void removeFromGroup(Group splineGroup);

  void addPathWaypoint(PathDisplayController controller);
}
