package edu.wpi.first.pathweaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

public class Path {
  private final List<Waypoint> waypoints = new ArrayList<>();
  private final String pathName;
  private int subchildIdx = 0;

  /**
   * Path constructor based on a known list of points.
   * @param newPoints   The list of waypoints to add
   * @param name        The name of the path
   */
  public Path(List<Waypoint> newPoints, String name) {
    pathName = name;
    waypoints.addAll(newPoints);
    for (int i = 1; i < waypoints.size(); i++) {
      Waypoint current = waypoints.get(i - 1);
      Waypoint next = waypoints.get(i);
      current.setSpline(new QuickSpline(current, next));
    }
    getEnd().setSpline(new NullSpline());
    for (Waypoint wp : waypoints) {
      wp.setPath(this);
    }
    updateSplines();
    enableSubchildSelector(subchildIdx);
  }

  public Path(String name) {
    this(new Point2D(0, 0), new Point2D(10, 10), new Point2D(10, 0),
        new Point2D(0, 10), name);
  }

  /**
   * Path constructor based on known start and end points.
   *
   * @param startPos        The starting waypoint of new path
   * @param endPos          The ending waypoint of new path
   * @param startTangent    The starting tangent vector of new path
   * @param endTangent      The ending tangent vector of new path
   * @param name            The string name to assign path, also used for naming exported files
   */
  public Path(Point2D startPos, Point2D endPos, Point2D startTangent, Point2D endTangent, String name) {
    pathName = name;
    Waypoint start = new Waypoint(startPos, startTangent, true, this);
    Waypoint end = new Waypoint(endPos, endTangent, true, this);
    start.setSpline(new QuickSpline(start, end));
    waypoints.add(start);
    waypoints.add(end);
    updateSplines();
  }

  @Override
  public String toString() {
    return getPathName();
  }

  /**
   * Creates new Waypoint in Path after previous.
   *
   * @param previous The node before this one
   * @param position Position to play new Waypoint
   * @param tangent  Tangent vector at the new point
   *
   * @return new Waypoint
   */
  public Waypoint addNewWaypoint(Waypoint previous, Point2D position, Point2D tangent, boolean locked) {
    Waypoint newPoint = new Waypoint(position, tangent, locked, this);
    if (previous == getEnd()) {
      previous.setSpline(new QuickSpline(previous, newPoint));
    } else {
      previous.getSpline().setEnd(newPoint);
    }
    waypoints.add(waypoints.indexOf(previous) + 1, newPoint);
    int nextPointIndex = waypoints.indexOf(newPoint) + 1;
    if (nextPointIndex < waypoints.size()) {
      newPoint.setSpline(new QuickSpline(newPoint, waypoints.get(nextPointIndex)));
    }
    newPoint.update();
    this.enableSubchildSelector(this.subchildIdx);
    updateSplines();
    return newPoint;
  }

  /**
   * Adds Waypoint in the middle of a spline.
   *
   * @param spline The spline to add point in middle of
   *
   * @return new Waypoint
   */
  public Waypoint addNewWaypoint(Spline spline) {
    for (int i = 1; i < waypoints.size(); i++) {
      Waypoint current = waypoints.get(i - 1);
      Waypoint next = waypoints.get(i);
      if (current.getSpline() == spline) {
        Point2D position = new Point2D(current.getX() + next.getX() / 2,
            (current.getY() + next.getY()) / 2);
        Point2D tangent = new Point2D(0, 0);
        return addNewWaypoint(current, position, tangent, false);
      }
    }
    return getEnd();
  }

  /**
   * Reflects the Path across an axis.
   * The coordinate system's origin is the starting point of the Path.
   * @param horizontal Flip over horizontal axis?
   * @param drawPane Pane to check validity of new points.
   */
  public void flip(boolean horizontal, Pane drawPane) {
    // Check if any new points are outside drawPane
    for (Waypoint wp : waypoints) {
      if (!drawPane.contains(reflectPoint(getStart(), wp, horizontal, false))) {
        invalidFlipAlert();
        return; // The new path is invalid
      }
    }
    // New waypoints are valid, update all Waypoints
    for (Waypoint wp : waypoints) {
      Point2D reflectedPos = reflectPoint(getStart(), wp, horizontal, false);
      Point2D reflectedTangent = reflectPoint(getStart(), wp, horizontal, true);
      wp.setX(reflectedPos.getX());
      wp.setY(reflectedPos.getY());
      wp.setTangent(reflectedTangent);
    }
    // Loop through to update points
    for (Waypoint wp : waypoints) {
      wp.update();
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

  public Waypoint getStart() {
    return waypoints.get(0);
  }

  public Waypoint getEnd() {
    return waypoints.get(waypoints.size() - 1);
  }

  /**
   * Calls update on all the Path's splines.
   */
  public void updateSplines() {
    for (Waypoint wp : waypoints) {
      wp.getSpline().update();
    }
  }

  /**
   * Forces recomputation of optimal theta value.
   * @param wp Waypoint to update theta for.
   */
  @SuppressWarnings("PMD.NcssCount")
  public void updateTheta(Waypoint wp) {
    Waypoint previous = waypoints.get(waypoints.indexOf(wp) - 1);
    Waypoint next = waypoints.get(waypoints.indexOf(wp) + 1);

    Point2D p1 = previous.getCoords();
    Point2D p2 = wp.getCoords();
    Point2D p3 = next.getCoords();

    Point2D p1Scaled = new Point2D(0, 0);
    Point2D p2Scaled = p2.subtract(p1).multiply(1 / p3.distance(p1));
    Point2D p3Shifted = p3.subtract(p1);
    Point2D p3Scaled = p3Shifted.multiply(1 / p3.distance(p1)); // scale

    //refactor later
    // Point2D q = new Point2D(0, 0); // for reference
    Point2D r = new Point2D(p2Scaled.getX() * p3Scaled.getX() + p2Scaled.getY() * p3Scaled.getY(),
        -p2Scaled.getX() * p3Scaled.getY() + p2Scaled.getY() * p3Scaled.getX());
    // Point2D s = new Point2D(1, 0); // for reference

    double beta = 1 - 2 * r.getX();
    double gamma = Math.pow(4 * (r.getX() - Math.pow(r.distance(p1Scaled), 2)) - 3, 3) / 27;
    double lambda = Math.pow(-gamma, 1 / 6);

    double phi1 = Math.atan2(Math.sqrt(-gamma - Math.pow(beta, 2)), beta) / 3;
    double ur = lambda * Math.cos(phi1);
    double ui = lambda * Math.sin(phi1);
    double phi2 = Math.atan2(-Math.sqrt(-gamma - Math.pow(beta, 2)), beta) / 3;

    double zr = lambda * Math.cos(phi2);
    double zi = lambda * Math.sin(phi2);

    double t1 = 1.0 / 2 + ur + zr / 2;
    double t2 = 1.0 / 2 - (1.0 / 4) * (ur + zr + Math.sqrt(3) * (ui - zi));
    double t3 = 1.0 / 2 - (1.0 / 4) * (ur + zr - Math.sqrt(3) * (ui - zi));

    double t;
    if (t1 > 0 && t1 < 1) {
      t = t1;
    } else if (t2 > 0 && t2 < 1) {
      t = t2;
    } else {
      t = t3;
    }

    Point2D a1 = p2.subtract(p1).subtract(p3Shifted.multiply(t)).multiply(1 / (t * t - t));
    Point2D a2 = p3Shifted.subtract(a1);

    Point2D tangent = a1.multiply(2 * t).add(a2).multiply(1. / 3);
    wp.setTangent(tangent);
  }

  /**
   * Returns all the tangent lines for the waypoints.
   * @return Collection of Tangent Lines.
   */
  public Collection<Node> getTangentLines() {
    Collection<Node> nodes = new ArrayList<>();
    for (Waypoint wp : waypoints) {
      nodes.add(wp.getTangentLine());
    }
    return nodes;
  }

  public List<Waypoint> getWaypoints() {
    return waypoints;
  }

  /**
   * Removes waypoint from path.
   * @param waypoint Waypoint to remove.
   */
  public void remove(Waypoint waypoint) {
    waypoints.remove(waypoint);
    for (int i = 1; i < waypoints.size(); i++) {
      Waypoint current = waypoints.get(i - 1);
      Waypoint next = waypoints.get(i);
      current.getSpline().setEnd(next);
      current.getSpline().update();
    }
  }


  /**
   * Enables a subchild class for all waypoints in this path.
   * @param i The index of subchild class to enable
   */
  public void enableSubchildSelector(int i) {
    this.subchildIdx = i;
    for (Waypoint wp : waypoints) {
      wp.enableSubchildSelector(i);
      wp.getSpline().enableSubchildSelector(i);
    }
  }

  /**
   * Duplicates a path.
   * @param newName Filename of new path.
   * @return The new path.
   */
  public Path duplicate(String newName) {
    Waypoint start = getStart();
    Waypoint end = getEnd();
    Path copy = new Path(start.getCoords(), end.getCoords(), start.getTangent(),
        end.getTangent(), newName);
    Waypoint insertPoint = copy.getStart();
    for (Waypoint wp : waypoints) {
      insertPoint = copy.addNewWaypoint(insertPoint, wp.getCoords(),
          wp.getTangent(), wp.isLockTangent());
    }
    return copy;
  }
}
