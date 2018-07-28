package edu.wpi.first.pathui;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

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
    if (previous.getNextWaypoint() != null) {
      newPoint.setNextWaypoint(previous.getNextWaypoint());
      previous.getNextWaypoint().setPreviousWaypoint(newPoint);
      previous.getNextWaypoint().getPreviousSpline().setStart(newPoint);
      newPoint.addSpline(previous.getNextWaypoint().getPreviousSpline(), true);
    }
    previous.setNextWaypoint(newPoint);
    createCurve(previous, newPoint); //new spline from new -> next
    if (previous == getEnd()) {
      setEnd(newPoint);
    }
    return newPoint;
  }


  /**
   * Reflects the Path across an axis.
   * The coordinate system's origin is the starting point of the Path.
   * @param horizontal Flip over horizontal axis?
   * @param drawPane Pane to check validity of new points.
   */
  public void flip(boolean horizontal, Pane drawPane) {
    // Check if any new points are outside drawPane
    Waypoint current = start;
    while (current != null) {
      if (!drawPane.contains(reflectPoint(start, current, horizontal, false))) {
        invalidFlipAlert();
        return; // The new path is invalid
      }
      current = current.getNextWaypoint();
    }
    // New waypoints are valid, update all Waypoints
    current = start;
    while (current != null) {
      Point2D reflectedPos = reflectPoint(start, current, horizontal, false);
      Point2D reflectedTangent = reflectPoint(start, current, horizontal, true);
      current.setCoords(reflectedPos);
      current.setTangent(reflectedTangent);
      current = current.getNextWaypoint();
    }
    // Loop through to update points
    current = start;
    while (current != null) {
      current.update();
      current = current.getNextWaypoint();
    }
  }

  private Point2D reflectPoint(Waypoint start, Waypoint point, boolean horizontal, boolean tangent) {
    Point2D coords;
    Point2D minus;
    if (tangent) {
      coords = point.getTangent();
      if (horizontal) {
        minus = new Point2D(coords.getX() * 2.0, 0.0);
      } else {
        minus = new Point2D(0.0, coords.getY() * 2.0);
      }
      return coords.subtract(minus);
    } else {
      coords = point.getCoords();
      if (horizontal) {
        minus = new Point2D(point.relativeTo(start).getX() * 2.0, 0.0);
      } else {
        minus = new Point2D(0.0, point.relativeTo(start).getY() * 2.0);
      }
      return coords.subtract(minus);
    }
  }

  private void invalidFlipAlert() {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle("");
    a.setHeaderText("The path could not be flipped.");
    String content = "Flipping this path would cause it to go out of bounds";
    a.setContentText(content);
    a.showAndWait();
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

  /**
   * Removes extension and version number from filename.
   * @return Filename without ".path" and version number.
   */
  public String getPathNameNoExtension() {
    String extension = ".path";
    String filename = pathName;
    if (pathName.endsWith(extension)) {
      filename = filename.substring(0, filename.length() - extension.length());
    }
    // remove version number
    filename = filename.replaceFirst("_[0-9]+", "");
    return filename;
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
  public void enableSubchildSelector(int i) {
    Waypoint next = getStart();
    while (next != null) {
      next.enableSubchildSelector(i);
      next = next.getNextWaypoint();
      if (next != null) {
        next.getPreviousSpline().enableSubchildSelector(i);
      }
    }
  }

  /**
   * Duplicates a path.
   * @param newName Filename of new path.
   * @return The new path.
   */
  public Path duplicate(String newName) {
    Path copy = new Path(start.getCoords(), end.getCoords(), start.getTangent(), end.getTangent(), newName);
    Waypoint oldPoint = start.getNextWaypoint();
    Waypoint insertPoint = copy.start;
    while (oldPoint != end) {
      insertPoint = copy.addNewWaypoint(insertPoint, oldPoint.getCoords(),
          oldPoint.getTangent(), oldPoint.isLockTangent());
      oldPoint = oldPoint.getNextWaypoint();
    }
    return copy;
  }
}
