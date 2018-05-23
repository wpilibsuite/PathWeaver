package edu.wpi.first.pathui;

import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class Waypoint {
  private Waypoint previousWaypoint = null;
  private Waypoint nextWaypoint = null;
  private final DoubleProperty xPixel = new SimpleDoubleProperty();
  private final DoubleProperty yPixel = new SimpleDoubleProperty();
  private final DoubleProperty theta = new SimpleDoubleProperty();
  private final DoubleProperty trueX = new SimpleDoubleProperty();
  private final DoubleProperty trueY = new SimpleDoubleProperty();
  private boolean lockTheta;
  private Spline previousSpline = null;
  private Spline nextSpline = null;
  private final ObjectProperty<Point2D> tangent = new SimpleObjectProperty<>();


  public static Waypoint currentWaypoint = null;
  private static DoubleProperty sceneWidth = new SimpleDoubleProperty();
  private static DoubleProperty sceneHeight = new SimpleDoubleProperty();


  private final Line tangentLine;
  private final Circle dot;
  private final EventHandler<MouseEvent> resetOnDoubleClick = event -> { //NOPMD
    if (event.getClickCount() == 2 && lockTheta) {
      lockTheta = false;
      update();
    }
  };

  /**
   * Creates Waypoint object containing javafx circle.
   *
   * @param xPosition  X coordinate in pixels
   * @param yPosition  Y coordinate in pixels
   * @param fixedAngle If the angle the of the waypoint should be fixed. Used for first and last waypoint
   */
  public Waypoint(double xPosition, double yPosition, boolean fixedAngle) {
    lockTheta = fixedAngle;
    trueX.set(xPosition);
    trueY.set(yPosition);

    xPixel.bind(Bindings.createDoubleBinding(() -> trueX.get() * getSceneWidth(), sceneWidth, trueX));
    yPixel.bind(Bindings.createDoubleBinding(() -> trueY.get() * getSceneHeight(), sceneHeight, trueY));

    dot = new Circle(10);
    dot.centerXProperty().bind(xPixel);
    dot.centerYProperty().bind(yPixel);
    xPixel.addListener(__ -> update());
    yPixel.addListener(__ -> update());

    tangentLine = new Line();
    tangentLine.startXProperty().bind(xPixel);
    tangentLine.startYProperty().bind(yPixel);
    tangent.set(new Point2D(0, 0));
    tangentLine.endXProperty().bind(Bindings.createObjectBinding(() ->
        getTangent().getX() * getSceneWidth() + getxPixel(), tangent, xPixel, sceneWidth));
    tangentLine.endYProperty().bind(Bindings.createObjectBinding(() ->
        getTangent().getY() * getSceneHeight() + getyPixel(), tangent, yPixel, sceneHeight));

    setupDnd();
  }

  private void setupDnd() {
    dot.setOnDragDetected(event -> {
      currentWaypoint = this;
      dot.startDragAndDrop(TransferMode.MOVE)
          .setContent(Map.of(DataFormats.WAYPOINT, "point"));
    });
    dot.setOnMouseClicked(resetOnDoubleClick);
    tangentLine.setOnDragDetected(event -> {
      currentWaypoint = this;
      tangentLine.startDragAndDrop(TransferMode.MOVE)
          .setContent(Map.of(DataFormats.CONTROL_VECTOR, "vector"));
    });
    tangentLine.setOnMouseClicked(resetOnDoubleClick);
  }

  public void lockTangent() {
    lockTheta = true;
  }

  /**
   * Updates the control points for the splines attached to this waypoint and to each of its neighbors.
   */
  public void update() {
    updateTheta();
    if (previousWaypoint != null) {
      previousWaypoint.updateTheta();
      getPreviousSpline().updateControlPoints();
      if (previousWaypoint.getPreviousSpline() != null) {
        previousWaypoint.getPreviousSpline().updateControlPoints();
      }
    }
    if (nextWaypoint != null) {
      nextWaypoint.updateTheta();
      getNextSpline().updateControlPoints();
      if (nextWaypoint.getNextSpline() != null) {
        nextWaypoint.getNextSpline().updateControlPoints();
      }
    }
  }

  /**
   * Forces Waypoint to recompute optimal theta value. Does nothing if lockTheta is true.
   */
  @SuppressWarnings("PMD.NcssCount")
  public void updateTheta() {
    if (lockTheta) {
      return;
    }
    if (previousWaypoint == null) {
      return;
    }
    if (nextWaypoint == null) {
      return;
    }
    //works better in consistent units or it assumes the shape is a square 0-1 by 0-1
    Point2D p1 = new Point2D(previousWaypoint.getxPixel(), previousWaypoint.getyPixel());
    Point2D p2 = new Point2D(getxPixel(), getyPixel());
    Point2D p3 = new Point2D(nextWaypoint.getxPixel(), nextWaypoint.getyPixel());

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

    Point2D trueTangent = new Point2D(tangent.getX() / getSceneWidth(), tangent.getY() / getSceneHeight());
    this.tangent.set(trueTangent);

    double newTheta = Math.atan2(getTangent().getY(), getTangent().getX());
    setTheta(newTheta);
  }

  /**
   * Sets previous or nextSpline and binds the Spline to waypoints position.
   *
   * @param newSpline The spline to add
   * @param amFirst   True if this waypoint is the first point in the spline
   */
  public void addSpline(Spline newSpline, boolean amFirst) {
    if (amFirst) {
      nextSpline = newSpline;
      nextSpline.getCubic().startXProperty().bind(xPixel);
      nextSpline.getCubic().startYProperty().bind(yPixel);
      newSpline.setStart(this);
    }
    if (!amFirst) {
      previousSpline = newSpline;
      previousSpline.getCubic().endXProperty().bind(xPixel);
      previousSpline.getCubic().endYProperty().bind(yPixel);
      newSpline.setEnd(this);
    }
  }

  public DoubleProperty trueXProperty() {
    return trueX;
  }

  public DoubleProperty trueYProperty() {
    return trueY;
  }

  public double getTrueX() {
    return trueX.get();
  }

  public void setTrueX(double trueX) {
    this.trueX.set(trueX);
  }

  public double getTrueY() {
    return trueY.get();
  }

  public void setTrueY(double trueY) {
    this.trueY.set(trueY);
  }

  public static double getSceneWidth() {
    return sceneWidth.get();
  }

  public static DoubleProperty sceneWidthProperty() {
    return sceneWidth;
  }

  public static void setSceneWidth(double sceneWidth) {
    Waypoint.sceneWidth.set(sceneWidth);
  }

  public static double getSceneHeight() {
    return sceneHeight.get();
  }

  public static DoubleProperty sceneHeightProperty() {
    return sceneHeight;
  }

  public static void setSceneHeight(double sceneHeight) {
    Waypoint.sceneHeight.set(sceneHeight);
  }

  public Line getTangentLine() {
    return tangentLine;
  }

  public Point2D getTangent() {
    return tangent.get();
  }

  public ObjectProperty<Point2D> tangentProperty() {
    return tangent;
  }

  public void setTangent(Point2D tangent) {
    this.tangent.set(tangent);
  }

  public Spline getPreviousSpline() {
    return previousSpline;
  }

  public Spline getNextSpline() {
    return nextSpline;
  }

  public double getTheta() {
    return theta.get();
  }

  public DoubleProperty thetaProperty() {
    return theta;
  }

  public void setTheta(double theta) {
    this.theta.set(theta);
  }

  public Circle getDot() {
    return dot;
  }

  public double getxPixel() {
    return xPixel.get();
  }

  public DoubleProperty xPixelProperty() {
    return xPixel;
  }

  public void setxPixel(double xPixel) {
    this.xPixel.set(xPixel);
  }

  public double getyPixel() {
    return yPixel.get();
  }

  public DoubleProperty yPixelProperty() {
    return yPixel;
  }

  public void setyPixel(double yPixel) {
    this.yPixel.set(yPixel);
  }

  public Waypoint getPreviousWaypoint() {
    return previousWaypoint;
  }

  public void setPreviousWaypoint(Waypoint previousWaypoint) {
    this.previousWaypoint = previousWaypoint;
  }

  public Waypoint getNextWaypoint() {
    return nextWaypoint;
  }

  public void setNextWaypoint(Waypoint nextWaypoint) {
    this.nextWaypoint = nextWaypoint;
  }

}
