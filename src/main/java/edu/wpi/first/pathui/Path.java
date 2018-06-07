package edu.wpi.first.pathui;

import javafx.geometry.Point2D;

public class Path {
  private Waypoint start;
  private Waypoint end;

  public Path() {
    createInitialWaypoints();
  }

  private void createInitialWaypoints() {
    start = new Waypoint(0, 0, false, this);
    start.setTheta(0);
    end = new Waypoint(250, 250, false, this);
    end.setTheta(3.14 / 2);
    start.setNextWaypoint(end);
    end.setPreviousWaypoint(start);
    start.setTangent(new Point2D(200, 0));
    end.setTangent(new Point2D(0, 200));
    createCurve(start, end);
    //setupWaypoint(start);
    //setupWaypoint(end);
  }

  /**
   * Make new Spline object.
   * @param start First Waypoint connected
   * @param end Second Waypoint connected
   * @return Spline object
   */
  public Spline createCurve(Waypoint start, Waypoint end) {
    Spline curve = new Spline(start, end);
    curve.getCubic().toBack();
    return curve;
  }

  /**
   * Add Waypoint between previous and next.
   * @param previous The point prior to new point
   * @param next The point after the new point
   * @return new Waypoint
   */
  public Waypoint addNewWaypoint(Waypoint previous, Waypoint next) {
    if (previous.getNextWaypoint() != next || next.getPreviousWaypoint() != previous) {
      throw new IllegalArgumentException("New Waypoint not between connected points");
    }
    Waypoint newPoint = new Waypoint((previous.getX() + next.getX()) / 2,
        (previous.getY() + next.getY()) / 2, false, this);
    newPoint.setPreviousWaypoint(previous);
    newPoint.setNextWaypoint(next);
    next.setPreviousWaypoint(newPoint);
    previous.setNextWaypoint(newPoint);
    //tell spline going from previous -> next to go from previous -> new
    newPoint.addSpline(previous.getNextSpline(), false);
    createCurve(newPoint, next); //new spline from new -> next

    newPoint.update();
    return newPoint;
  }

  public Waypoint getStart() {
    return start;
  }

  public Waypoint getEnd() {
    return end;
  }
}
