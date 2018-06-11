package edu.wpi.first.pathui;

import javafx.geometry.Point2D;

public class Path {
  private Waypoint start;
  private Waypoint end;

  public Path() {
    createDefaultWaypoints();
  }

  public Path(Point2D start, Point2D end, Point2D startTangent, Point2D endTangent) {
    createInitialWaypoints(start, end, startTangent, endTangent);

  }


  private void createDefaultWaypoints() {
    Point2D startPos = new Point2D(0, 0);
    Point2D endPos = new Point2D(250, 250);

    Point2D startTangent = new Point2D(200, 0);
    Point2D endTangent = new Point2D(0, 200);
    createInitialWaypoints(startPos, endPos, startTangent, endTangent);
  }

  private void createInitialWaypoints(Point2D startPos, Point2D endPos, Point2D startTangent, Point2D endTangent) {
    start = new Waypoint(startPos, startTangent, false, this);
    end = new Waypoint(endPos, endTangent, false, this);

    start.setNextWaypoint(end);
    end.setPreviousWaypoint(start);
    createCurve(start, end);
  }

  /**
   * Make new Spline object.
   *
   * @param start First Waypoint connected
   * @param end   Second Waypoint connected
   *
   * @return Spline object
   */
  public Spline createCurve(Waypoint start, Waypoint end) {
    Spline curve = new Spline(start, end);
    curve.getCubic().toBack();
    return curve;
  }

  /**
   * Add Waypoint between previous and next.
   *
   * @param previous The point prior to new point
   * @param next     The point after the new point
   *
   * @return new Waypoint
   */
  public Waypoint addNewWaypoint(Waypoint previous, Waypoint next) {
    if (previous.getNextWaypoint() != next || next.getPreviousWaypoint() != previous) {
      throw new IllegalArgumentException("New Waypoint not between connected points");
    }
    Point2D position = new Point2D(previous.getX() + next.getX() / 2, (previous.getY() + next.getY()) / 2);
    Point2D tangent = new Point2D(0, 0);
    Waypoint newPoint = new Waypoint(position, tangent, false, this);
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
