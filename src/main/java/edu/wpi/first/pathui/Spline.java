package edu.wpi.first.pathui;

import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.CubicCurve;
import javafx.util.Pair;

public class Spline {
  private final CubicCurve cubic;
  private Waypoint start;
  private Waypoint end;

  public static Spline currentSpline = null;

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


    cubic.setOnDragDetected(event -> {
      Dragboard board = cubic.startDragAndDrop(TransferMode.ANY);
      board.setContent(Map.of(DataFormats.SPLINE, "Spline"));
      currentSpline = this;
    });
  }

  public void enableSubchildSelector(int i) {
    FxUtils.enableSubchildSelector(cubic, i);
    cubic.applyCss();
  }

  /**
   * Forces Spline to recompute and update its bezier curve control points.
   */
  public void updateControlPoints() {
    Pair<Point2D, Point2D> points = computeControlPoints();
    cubic.setControlX1(points.getKey().getX());
    cubic.setControlY1(points.getKey().getY());
    cubic.setControlX2(points.getValue().getX());
    cubic.setControlY2(points.getValue().getY());
  }

  // Bezier curve to hermite curve
  // p1 p2 p3 p4 for bezier curve
  // heading 1 = 3 * (p2 - p1)
  // heading 4 = 3 * (p4 - p3)
  private Pair<Point2D, Point2D> computeControlPoints() {
    Point2D control1 = new Point2D(start.getX() + (start.getTangent().getX()) / 3,
        start.getY() + (start.getTangent().getY()) / 3);
    Point2D control2 = new Point2D(end.getX() - 2 * (end.getTangent().getX()) / 3,
        end.getY() - 2 * (end.getTangent().getY()) / 3);
    return new Pair<>(control1, control2);
  }

  public CubicCurve getCubic() {
    return cubic;
  }

  public Waypoint getStart() {
    return start;
  }

  public void setStart(Waypoint start) {
    this.start = start;
  }

  public Waypoint getEnd() {
    return end;
  }

  public void setEnd(Waypoint end) {
    this.end = end;
  }
}
