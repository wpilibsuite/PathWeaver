package edu.wpi.first.pathui;

import javafx.geometry.Point2D;

public class Path {
  private Waypoint start;
  private Waypoint end;
  private String pathName = "default";

  public Path(String name) {
    pathName = name;
    createDefaultWaypoints();
  }

  /**
   * Path constructor based on known start and end points.
   *
   * @param start        The starting waypoint of new path
   * @param end          The ending waypoint of new path
   * @param startTangent The starting tangent vector of new path
   * @param endTangent   The ending tangent vector of new path
   * @param name         The string name to assign path, also used for naming exported files
   */
  public Path(Point2D start, Point2D end, Point2D startTangent, Point2D endTangent, String name) {
    pathName = name;
    createInitialWaypoints(start, end, startTangent, endTangent);
  }

  @Override
  public String toString() {
    return getPathName();
  }

  private void createDefaultWaypoints() {
    Point2D startPos = new Point2D(0, 0);
    Point2D endPos = new Point2D(10, 10);

    Point2D startTangent = new Point2D(10, 0);
    Point2D endTangent = new Point2D(0, 10);
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

  /**
   * Reflects the Path across the X-axis.
   * The coordinate system's origin is the starting point of the Path.
   */
  public void flipVertical() {
    Waypoint current = start;
    while (current != null) {
      Point2D reflectedPos = current.getCoords().subtract(
          new Point2D(0.0, current.relativeTo(start).getY() * 2));
      Point2D reflectedTangent = current.getTangent().subtract(
          new Point2D(0.0, current.getTangent().getY() * 2));
      current.setCoords(reflectedPos);
      current.setTangent(reflectedTangent);
      current = current.getNextWaypoint();
    }
  }

  /**
   * Reflects the Path across the X-axis.
   * The coordinate system's origin is the starting point of the Path.
   */
  public void flipHorizontal() {
    Waypoint current = start;
    while (current != null) {
      Point2D reflectedPos = current.getCoords().subtract(
          new Point2D(current.relativeTo(start).getX() * 2, 0.0));
      Point2D reflectedTangent = current.getTangent().subtract(
          new Point2D(current.getTangent().getX() * 2, 0.0));
      current.setCoords(reflectedPos);
      current.setTangent(reflectedTangent);
      current = current.getNextWaypoint();
    }
  }

  /**
   * Convenience function for debugging purposes.
   *
   * @return A nicely formatted String representing some of the data stored in the Waypoint class.
   */
  public String getPointString() {
    StringBuilder sb = new StringBuilder();
    Waypoint current = start;
    while (current != null) {
      sb.append(String.format("X: %s\tY:%s\n", current.getX(), current.getY()));
      current = current.getNextWaypoint();
    }
    return sb.toString();
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

  /**
   * Enables a subchild class for all waypoints in this path.
   * @param i The index of subchild class to enable
   */
  public void enableSubchildClass(int i) {
    Waypoint next = getStart();
    while (next != null) {
      next.enableSubchildClass(i);
      next = next.getNextWaypoint();
      if (next != null) {
        next.getPreviousSpline().updateControlPoints();
      }
    }
  }
}
