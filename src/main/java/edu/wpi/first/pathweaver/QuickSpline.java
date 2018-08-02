package edu.wpi.first.pathweaver;

import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.CubicCurve;
import javafx.util.Pair;

/**
 * A QuickSpline is represented by a JavaFX CubicCurve. A slow spline will be implemented using
 * Pathfinder V2.
 */
public class QuickSpline implements Spline {
  private final Waypoint start;
  private Waypoint end;
  private final CubicCurve curve;

  @Override
  public void setEnd(Waypoint end) {
    this.end = end;
    update();
  }

  @Override
  public void enableSubchildSelector(int i) {
    FxUtils.enableSubchildSelector(curve, i);
    curve.applyCss();
  }

  @Override
  public void removeFromGroup(Group splineGroup) {
    splineGroup.getChildren().remove(curve);
  }

  @Override
  public void addPathWaypoint(PathDisplayController controller) {
    Waypoint newPoint = start.getPath().addNewWaypoint(this);
    controller.addWaypointToPane(newPoint);
    controller.setupWaypoint(newPoint);
    controller.selectWaypoint(newPoint, false);
    Waypoint.currentWaypoint = newPoint;
  }

  /**
   * Constructs a QuickSpline.
   * @param start The waypoint at start of spline.
   * @param end The waypoint at the end of spline.
   */
  public QuickSpline(Waypoint start, Waypoint end) {
    this.start = start;
    this.end = end;
    curve = new CubicCurve();
    curve.setOnDragDetected(event -> {
      Dragboard board = curve.startDragAndDrop(TransferMode.ANY);
      board.setContent(Map.of(DataFormats.SPLINE, "Spline"));
      DragHandler.setCurrentSpline(this);
    });
    curve.getStyleClass().add("path");
    FxUtils.applySubchildClasses(curve);
  }

  private Pair<Point2D, Point2D> computeControlPoints() {
    // Bezier curve to hermite curve
    // p1 p2 p3 p4 for bezier curve
    // heading 1 = 3 * (p2 - p1)
    // heading 4 = 3 * (p4 - p3)
    Point2D control1 = new Point2D(start.getX() + (start.getTangent().getX()) / 3,
        start.getY() + (start.getTangent().getY()) / 3);
    Point2D control2 = new Point2D(end.getX() - 2 * (end.getTangent().getX()) / 3,
        end.getY() - 2 * (end.getTangent().getY()) / 3);
    return new Pair<>(control1, control2);
  }

  @Override
  public void update() {
    Pair<Point2D, Point2D> points = computeControlPoints();
    curve.startXProperty().bind(start.xProperty());
    curve.startYProperty().bind(start.yProperty());
    curve.endXProperty().bind(end.xProperty());
    curve.endYProperty().bind(end.yProperty());
    curve.setControlX1(points.getKey().getX());
    curve.setControlY1(points.getKey().getY());
    curve.setControlX2(points.getValue().getX());
    curve.setControlY2(points.getValue().getY());
  }

  @Override
  public void addToGroup(Group splineGroup, double scaleFactor) {
    splineGroup.getChildren().add(curve);
    curve.toBack();
    curve.setStrokeWidth(scaleFactor);
  }
}
