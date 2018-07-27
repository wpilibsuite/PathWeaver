package edu.wpi.first.pathui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

public class Path {
  private LinkedList<Waypoint> waypoints = new LinkedList<>();
  private ArrayList<Spline> splines = new ArrayList<>();
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
    Waypoint start = new Waypoint(startPos, startTangent, false, this);
    Waypoint end = new Waypoint(endPos, endTangent, false, this);
    waypoints.add(start);
    waypoints.add(end);
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
    if (waypoints.indexOf(previous) != waypoints.indexOf(next) - 1) {
      throw new IllegalArgumentException("New Waypoint not between connected points");
    }
    Point2D position = new Point2D(previous.getX() + next.getX() / 2, (previous.getY() + next.getY()) / 2);
    Point2D tangent = new Point2D(0, 0);

    //add new point after previous
    Waypoint newPoint = addNewWaypoint(previous, position, tangent, false);

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
    waypoints.add(waypoints.indexOf(previous) + 1, newPoint);
    createCurve(previous, newPoint); //new spline from new -> next
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
      wp.setCoords(reflectedPos);
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

  /**
   * Convenience function for debugging purposes.
   *
   * @return A nicely formatted String representing some of the data stored in the Waypoint class.
   */
  public String getPointString() {
    StringBuilder sb = new StringBuilder();
    for (Waypoint wp : waypoints) {
      sb.append(String.format("X: %s\tY:%s\n", wp.getX(), wp.getY()));
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
    return waypoints.getFirst();
  }

  public Waypoint getEnd() {
    return waypoints.getLast();
  }

  public void updatePoint(Waypoint wp) {
    if (getStart() != wp) {
      Waypoint previous = waypoints.get(waypoints.indexOf(wp) - 1);
      wp.getPreviousSpline().updateControlPoints();
      if (previous.getPreviousSpline() != null) {
        previous.getPreviousSpline().updateControlPoints();
      }
    }
    if (getEnd() != wp) {
      Waypoint next = waypoints.get(waypoints.indexOf(wp) + 1);
      wp.getNextSpline().updateControlPoints();
      if (next.getNextSpline() != null) {
        next.getNextSpline().updateControlPoints();
      }
    }
  }

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

  public Collection<Node> getTangentLines() {
    Collection<Node> nodes = new ArrayList<>();
    for (Waypoint wp : waypoints) {
      nodes.add(wp.getTangentLine());
    }
    return nodes;
  }

  public LinkedList<Waypoint> getWaypoints() {
    return waypoints;
  }

  public void remove(Waypoint waypoint) {
    waypoints.remove(waypoint);
  }
}
