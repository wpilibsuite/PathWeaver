package edu.wpi.first.pathui;

import javafx.geometry.Point2D;

public class Path {
  private Waypoint start;
  private Waypoint end;
  private String pathName = "default";

  public Path() {
    createDefaultWaypoints();
  }

  /** Path constructor based on known start and end points.
   *
   * @param start The starting waypoint of new path
   * @param end The ending waypoint of new path
   * @param startTangent The starting tangent vector of new path
   * @param endTangent The ending tangent vector of new path
   * @param name The string name to assign path, also used for naming exported files
   */
  public Path(Point2D start, Point2D end, Point2D startTangent, Point2D endTangent, String name) {
    pathName = name;
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

    //add new point after previous
    Waypoint newPoint = addNewWaypoint(previous, position, tangent, false);

    //connect newPoint to next
    newPoint.setNextWaypoint(next);
    next.setPreviousWaypoint(newPoint);
    //tell spline going from previous -> next to go from previous -> new
    newPoint.addSpline(next.getPreviousSpline(), true);
    newPoint.update();
    return newPoint;
  }

  /**
   * Create new Waypoint in Path after previous.
   *
   * @param previous The node before this one
   * @param position Position to play new Waypoint
   * @param tangent  Tangent vector at the new point
   *
   * @return new Waypoint
   */
  public Waypoint addNewWaypoint(Waypoint previous, Point2D position, Point2D tangent, Boolean locked) {
    Waypoint newPoint = new Waypoint(position, tangent, locked, this);
    newPoint.setPreviousWaypoint(previous);
    previous.setNextWaypoint(newPoint);
    createCurve(previous, newPoint); //new spline from new -> next
    if (previous == getEnd()) {
      setEnd(newPoint);
    }
    return newPoint;
  }

  public String getPathName() {
    return pathName;
  }

  public void setPathName(String pathName) {
    this.pathName = pathName;
  }

  public Waypoint getStart() {
    return start;
  }

  public Waypoint getEnd() {
    return end;
  }

  public void setEnd(Waypoint end) {
    this.end = end;
  }
}
