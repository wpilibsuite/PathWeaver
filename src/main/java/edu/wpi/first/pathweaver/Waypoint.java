package edu.wpi.first.pathweaver;

import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class Waypoint implements PropertyManager.PropertyEditable {
  private final DoubleProperty x = new SimpleDoubleProperty();
  private final DoubleProperty y = new SimpleDoubleProperty();
  private final DoubleProperty tangentX = new SimpleDoubleProperty();
  private final DoubleProperty tangentY = new SimpleDoubleProperty();
  private final SimpleBooleanProperty lockTangent = new SimpleBooleanProperty();
  private Spline spline;

  private final Path path;
  public static Waypoint currentWaypoint = null;


  private final Line tangentLine;
  private Polygon icon;

  private static final double SIZE = 30.0;

  private final ObservableList<PropertyManager.NamedProperty> properties = FXCollections.observableArrayList();


  public Path getPath() {
    return path;
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

    tangentLine = new Line();
    tangentLine.getStyleClass().add("tangent");
    tangentLine.startXProperty().bind(x);
    tangentLine.startYProperty().bind(y);
    setTangent(tangentVector);
    tangentLine.endXProperty().bind(Bindings.createObjectBinding(() -> tangentX.get() + getX(), tangentX, x));
    tangentLine.endYProperty().bind(Bindings.createObjectBinding(() -> tangentY.get() + getY(), tangentY, y));

    setupProperties();

    this.spline = new NullSpline();

    setupDnd();
  }

  private void setupProperties() {
    properties.add(new PropertyManager.NamedProperty<Double>("X", xProperty()));
    properties.add(new PropertyManager.NamedProperty<Double>("Y", yProperty()));
    properties.add(new PropertyManager.NamedProperty<Double>("Tangent Vector X", tangentX, (newVal) -> {
      lockTangent();
      return true;
    }));
    properties.add(new PropertyManager.NamedProperty<Double>("Tangent Vector Y", tangentY, (newVal) -> {
      lockTangent();
      return true;
    }));
    properties.add(new PropertyManager.NamedProperty<Double>("Lock Tangent", lockTangent));
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

  public void lockTangent() {
    lockTangent.set(true);
  }

  /**
   * Updates the control points for the splines attached to this waypoint and to each of its neighbors.
   */
  public void update() {
    if (this != path.getStart() && this != path.getEnd()) {
      updateTheta();
    }
    path.updateSplines();
  }

  /**
   * Forces Waypoint to recompute optimal theta value. Does nothing if lockTangent is true.
   */
  public void updateTheta() {
    if (!isLockTangent()) {
      path.updateTheta(this);
    }
  }



  /**
   * Convenience function for math purposes.
   *
   * @param other The other Waypoint.
   *
   * @return The coordinates of this Waypoint relative to the coordinates of another Waypoint.
   */
  public Point2D relativeTo(Waypoint other) {
    return relativeTo(other.getCoords());
  }

  /**
   * Convenience function allowing us to obtain the position of this Waypoint relative to a Point2S.
   *
   * @param other The other Point2D.
   *
   * @return A Point2D representing the distance between this Watpoint and a given Point2D.
   */
  public Point2D relativeTo(Point2D other) {
    return new Point2D(this.getX() - other.getX(), this.getY() - other.getY());
  }

  public boolean isLockTangent() {
    return lockTangent.get();
  }

  public Line getTangentLine() {
    return tangentLine;
  }

  public void setTangent(Point2D tangent) {
    this.tangentX.set(tangent.getX());
    this.tangentY.set(tangent.getY());
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

  public void setCoords(Point2D newCoords) {
    setX(newCoords.getX());
    setY(newCoords.getY());
  }

  public void setSpline(Spline spline) {
    this.spline = spline;
  }

  public Spline getSpline() {
    return spline;
  }

  @Override
  public ObservableList<PropertyManager.NamedProperty> getProperties() {
    return this.properties;
  }

  public Point2D getTangent() {
    return new Point2D(tangentX.get(), tangentY.get());
  }
}
