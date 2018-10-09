package edu.wpi.first.pathweaver;

import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Waypoint {
  private final DoubleProperty x = new SimpleDoubleProperty();
  private final DoubleProperty y = new SimpleDoubleProperty();
  private final DoubleProperty tangentX = new SimpleDoubleProperty();
  private final DoubleProperty tangentY = new SimpleDoubleProperty();
  private final BooleanProperty lockTangent = new SimpleBooleanProperty();
  private final StringProperty name = new SimpleStringProperty("");

  private Spline spline;

  private Path path;
  public static Waypoint currentWaypoint = null;

  private final Rectangle robotOutline;
  private final Line tangentLine;
  private Polygon icon;

  private static final double SIZE = 30.0;
  private final Tooltip nameTip = new Tooltip();


  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  /**
   * Creates Waypoint object containing javafx circle.
   *
   * @param position      x and y coordinates in user set units
   * @param tangentVector tangent vector in user set units
   * @param fixedAngle    If the angle the of the waypoint should be fixed. Used for first and last waypoint
   * @param myPath        the path this waypoint belongs to
   */
  public Waypoint(Point2D position, Point2D tangentVector, boolean fixedAngle, Path myPath) {
    path = myPath;
    lockTangent.set(fixedAngle);
    setX(position.getX());
    setY(position.getY());
    icon = new Polygon();
    setupIcon();
    x.addListener(__ -> update());
    y.addListener(__ -> update());

    ProjectPreferences.Values values = ProjectPreferences.getInstance().getValues();

    tangentLine = new Line();
    tangentLine.getStyleClass().add("tangent");
    tangentLine.startXProperty().bind(x);
    tangentLine.startYProperty().bind(y);
    setTangent(tangentVector);
    tangentLine.endXProperty().bind(Bindings.createObjectBinding(() -> getTangent().getX() + getX(), tangentX, x));
    tangentLine.endYProperty().bind(Bindings.createObjectBinding(() -> getTangent().getY() + getY(), tangentY, y));

    double robotWidth = values.getRobotWidth() / 12;
    double robotLength = values.getRobotLength() / 12;

    robotOutline = new Rectangle();
    robotOutline.setHeight(robotWidth);
    robotOutline.setWidth(robotLength);
    robotOutline.xProperty().bind(x.subtract(robotLength / 2));
    robotOutline.yProperty().bind(y.subtract(robotWidth / 2));
    robotOutline.rotateProperty().bind(
            Bindings.createObjectBinding(() ->
                    getTangent() == null ? 0.0 : Math.toDegrees(Math.atan2(getTangent().getY(), getTangent().getX())),
                    tangentX, tangentY));

    this.spline = new NullSpline();

    setupDnd();
  }

  /**
   * Creates waypoint before a path is created. Call setPath() once path is created.
   * @param position      x and y coordinates in user set units
   * @param tangentVector tangent vector in user set units
   * @param fixedAngle    If the angle the of the waypoint should be fixed. Used for first and last waypoint
   */
  public Waypoint(Point2D position, Point2D tangentVector, boolean fixedAngle) {
    this(position, tangentVector, fixedAngle, null);
  }


  public void enableSubchildSelector(int i) {
    FxUtils.enableSubchildSelector(this.icon, i);
    getIcon().applyCss();
  }

  private void setupIcon() {
    icon = new Polygon(
            0.0, SIZE / 3,
            SIZE, 0.0,
            0.0, -SIZE / 3);
    double xOffset = (SIZE * 3D / 5D) / 16.5;
    icon.setLayoutX(-(icon.getLayoutBounds().getMaxX() + icon.getLayoutBounds().getMinX()) / 2 - xOffset);
    icon.setLayoutY(-(icon.getLayoutBounds().getMaxY() + icon.getLayoutBounds().getMinY()) / 2);

    icon.translateXProperty().bind(x);
    icon.translateYProperty().bind(y);
    FxUtils.applySubchildClasses(this.icon);
    this.icon.rotateProperty().bind(
            Bindings.createObjectBinding(() ->
                    getTangent() == null ? 0.0 : Math.toDegrees(Math.atan2(getTangent().getY(), getTangent().getX())),
                    tangentX, tangentY));
    nameTip.setShowDelay(Duration.millis(200));
    nameTip.textProperty().bind(name);
    icon.getStyleClass().add("waypoint");
  }

  private void setupDnd() {
    icon.setOnDragDetected(event -> {
      currentWaypoint = this;
      icon.startDragAndDrop(TransferMode.MOVE)
          .setContent(Map.of(DataFormats.WAYPOINT, "point"));
    });
    tangentLine.setOnDragDetected(event -> {
      currentWaypoint = this;
      tangentLine.startDragAndDrop(TransferMode.MOVE)
          .setContent(Map.of(DataFormats.CONTROL_VECTOR, "vector"));
    });
    tangentLine.setOnMouseClicked(event -> {
      resetOnDoubleClick(event);
    });
  }

  /**
   * Handles reseting point depending on the mouse event.
   *
   * @param event The mouse event that was triggered
   */
  public void resetOnDoubleClick(MouseEvent event) {
    if (event.getClickCount() == 2 && lockTangent.get()) {
      lockTangent.set(false);
      update();
    }
  }

  /**
   * Updates the control points for the splines attached to this waypoint and to each of its neighbors.
   */
  public void update() {
    if (this != path.getStart() && this != path.getEnd() && !isLockTangent()) {
      path.updateTheta(this);
    }
    path.updateSplines();
  }

  /**
   * Convenience function for math purposes.
   *
   * @param other The other Waypoint.
   *
   * @return The coordinates of this Waypoint relative to the coordinates of another Waypoint.
   */
  public Point2D relativeTo(Waypoint other) {
    return new Point2D(this.getX() - other.getX(), this.getY() - other.getY());
  }

  public boolean isLockTangent() {
    return lockTangent.get();
  }

  public BooleanProperty lockTangentProperty() {
    return lockTangent;
  }

  public Line getTangentLine() {
    return tangentLine;
  }

  public Point2D getTangent() {
    return new Point2D(tangentX.get(), tangentY.get());
  }

  public void setTangent(Point2D tangent) {
    this.tangentX.set(tangent.getX());
    this.tangentY.set(tangent.getY());
  }

  public Rectangle getRobotOutline() {
    return robotOutline;
  }

  public Polygon getIcon() {
    return icon;
  }

  public double getX() {
    return x.get();
  }

  public DoubleProperty xProperty() {
    return x;
  }

  public void setX(double x) {
    this.x.set(x);
  }

  public double getY() {
    return y.get();
  }

  public DoubleProperty yProperty() {
    return y;
  }

  public void setY(double y) {
    this.y.set(y);
  }

  public Point2D getCoords() {
    return new Point2D(getX(), getY());
  }

  public void setSpline(Spline spline) {
    this.spline = spline;
  }

  public Spline getSpline() {
    return spline;
  }

  public String getName() {
    return name.get();
  }

  public StringProperty nameProperty() {
    return name;
  }

  /**
   * Updates the Waypoint name and configures the icon tooltip.
   * @param name New name of Waypoint.
   */
  public void setName(String name) {
    if (name.isEmpty()) {
      Tooltip.uninstall(icon, nameTip);
    } else if (this.name.get().isEmpty()) {
      Tooltip.install(icon, nameTip);
    }
    this.name.set(name);
  }

  public DoubleProperty tangentXProperty() {
    return tangentX;
  }

  public DoubleProperty tangentYProperty() {
    return tangentY;
  }
}
