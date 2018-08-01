package edu.wpi.first.pathweaver;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

// Should be refactored if an obvious distinction is found or method count grows too much more
@SuppressWarnings("PMD.TooManyMethods")
public class PathDisplayController {
  @FXML
  private ImageView backgroundImage;
  @FXML
  private Pane drawPane;
  @FXML
  private Group group;
  @FXML
  private Pane topPane;
  private final PseudoClass selected = PseudoClass.getPseudoClass("selected");

  private final Property<Waypoint> selectedWaypointProp = new SimpleObjectProperty<>(); //NOPMD
  private final ObjectProperty<Path> currentPath = new SimpleObjectProperty<>();
  private final Field field = new Field();

  private final ObservableList<Path> pathList = FXCollections.observableArrayList();
  private String pathDirectory;

  private final double circleScale = .75; //NOPMD should be static, will be modified later
  private final double splineScale = 6; //NOPMD should be static, will be modified later
  private final double lineScale = 2; //NOPMD should be static, will be modified later

  @FXML
  private Group splineGroup;
  @FXML
  private Group waypointGroup;
  @FXML
  private Group vectorGroup;

  @FXML
  private void initialize() {

    Image image = field.getImage();
    backgroundImage.setImage(image);
    Scale scale = new Scale();
    scale.xProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
        topPane.widthProperty(), topPane.heightProperty()));
    scale.yProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
        topPane.widthProperty(), topPane.heightProperty()));

    group.getTransforms().add(scale);
    setupDrawPaneSizing(image);
    new DragHandler(this, drawPane); // Handler doesn't need to be kept around by this, so just do setup
    setupPress();
    setupPathListeners();
  }

  private void setupPathListeners() {
    pathList.addListener((ListChangeListener<Path>) change -> {
      while (change.next()) {
        for (Object o : change.getRemoved()) {
          Path path = (Path) o;
          removePathFromPane(path);
        }
        for (Object o : change.getAddedSubList()) {
          Path path = (Path) o;
          addPathToPane(path);
        }
      }
    });
    currentPath.addListener((change, oldValue, newValue) -> {
      vectorGroup.getChildren().clear();
      if (newValue == null) {
        return;
      }
      Waypoint nextPoint = newValue.getStart();
      while (nextPoint != null) {
        vectorGroup.getChildren().add(nextPoint.getTangentLine());
        nextPoint = nextPoint.getNextWaypoint();
      }
    });
  }

  /**
   * Adds a path to the controller.
   * @param fileLocations The folder containing the path file
   * @param newValue The TreeItem holding the name of this path
   * @return The new path, or if duplicate, the old path matching the file name
   */
  public Path addPath(String fileLocations, TreeItem<String> newValue) {
    String fileName = newValue.getValue();
    for (Path path : pathList) {
      if (fileName.equals(path.getPathName())) {
        return path;
      }
    }
    Path newPath = PathIOUtil.importPath(fileLocations, fileName);
    if (newPath == null) {
      newPath = new Path(fileName);
      PathIOUtil.export(fileLocations, newPath);
    }
    pathList.add(newPath);
    return newPath;
  }

  /**
   * Remove all paths from Controller.
   */
  public void removeAllPath() {
    selectedWaypointProp.setValue(null);
    pathList.clear();
  }


  //between this and above public function better names could be found
  private void addPathToPane(Path newPath) {
    Waypoint current = newPath.getStart();
    while (current != null) {
      setupWaypoint(current);
      addWaypointToPane(current);
      current = current.getNextWaypoint();
    }
    currentPath.set(newPath);
  }

  /**
   * Adds a waypoint to the drawing pane.
   *
   * @param current The waypoint
   */
  public void addWaypointToPane(Waypoint current) {
    waypointGroup.getChildren().add(current.getIcon());
    vectorGroup.getChildren().add(current.getTangentLine());
    current.getIcon().setScaleX(circleScale / field.getScale());
    current.getIcon().setScaleY(circleScale / field.getScale());
    current.getTangentLine().setStrokeWidth(lineScale / field.getScale());
    current.getTangentLine().toBack();
    if (current != null && current.getPreviousWaypoint() != null) {
      splineGroup.getChildren().add(current.getPreviousSpline().getCubic());
      current.getPreviousSpline().getCubic().toBack();
      current.getPreviousSpline().getCubic().setStrokeWidth(splineScale / field.getScale());
    }
    if (current != null && current.getNextSpline() != null) {
      current.getNextSpline().getCubic().toBack();
    }
  }


  private void removePathFromPane(Path newPath) {
    Waypoint current = newPath.getStart();
    while (current != null) {
      waypointGroup.getChildren().remove(current.getIcon());
      vectorGroup.getChildren().remove(current.getTangentLine());
      current = current.getNextWaypoint();
      if (current != null) {
        splineGroup.getChildren().remove(current.getPreviousSpline().getCubic());
      }
    }
  }

  private void setupDrawPaneSizing(Image image) {
    drawPane.setMaxWidth(image.getWidth());
    drawPane.setMaxHeight(image.getHeight());
    drawPane.setPrefHeight(field.getRealLength().getValue().doubleValue());
    drawPane.setPrefWidth(field.getRealWidth().getValue().doubleValue());
    drawPane.setLayoutX(field.getCoord().getX());
    drawPane.setLayoutY(field.getCoord().getY());
    drawPane.setScaleX(field.getScale());
    drawPane.setScaleY(field.getScale());
  }

  /**
   * Setup fx interactions for the given waypoint object.
   *
   * @param waypoint The waypoint
   */
  public void setupWaypoint(Waypoint waypoint) {
    waypoint.getIcon().setOnMouseClicked(e -> {
          waypoint.resetOnDoubleClick(e);
          if (e.getClickCount() == 1) {
            selectWaypoint(waypoint, true);
          }
          e.consume();
        }
    );

    waypoint.getIcon().setOnContextMenuRequested(e -> {
      ContextMenu menu = new ContextMenu();
      if (isDeletable(waypoint)) {
        menu.getItems().add(FxUtils.menuItem("Delete", __ -> delete(waypoint)));
      }
      if (waypoint.getTangentLine().isVisible()) {
        menu.getItems().add(FxUtils.menuItem("Hide control vector", __ -> waypoint.getTangentLine().setVisible(false)));
      } else {
        menu.getItems().add(FxUtils.menuItem("Show control vector", __ -> waypoint.getTangentLine().setVisible(true)));
      }
      menu.show(drawPane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
  }

  @FXML
  private void keyPressed(KeyEvent event) {
    if ((event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)
        && isDeletable(selectedWaypointProp.getValue())) {
      delete(selectedWaypointProp.getValue());
    }
  }


  private boolean isDeletable(Waypoint waypoint) {
    return waypoint.getPreviousWaypoint() != null
        && waypoint.getNextWaypoint() != null;
  }

  private void delete(Waypoint waypoint) {
    Waypoint previousWaypoint = waypoint.getPreviousWaypoint();
    Waypoint nextWaypoint = waypoint.getNextWaypoint();
    waypointGroup.getChildren().remove(waypoint.getIcon());
    vectorGroup.getChildren().remove(waypoint.getTangentLine());
    splineGroup.getChildren().remove(waypoint.getPreviousSpline().getCubic());
    splineGroup.getChildren().remove(waypoint.getNextSpline().getCubic());
    previousWaypoint.setNextWaypoint(nextWaypoint);
    nextWaypoint.setPreviousWaypoint(previousWaypoint);
    Spline newCurve = previousWaypoint.getPath().createCurve(previousWaypoint, nextWaypoint);
    newCurve.getCubic().setStrokeWidth(splineScale / field.getScale());
    splineGroup.getChildren().add(newCurve.getCubic());
    newCurve.getCubic().toBack();
    previousWaypoint.update();
    nextWaypoint.update();
    PathIOUtil.export(pathDirectory, previousWaypoint.getPath());
  }

  /**
   * Selects or deselects a waypoint and associated path for the purposes of drawing, dragging, and otherwise modifying
   * If toggle is true, then deselect the waypoint if it is the same as the currently selected waypoint.
   *
   * @param waypoint The waypoint to be selected
   * @param toggle   Whether to toggle the selection if possible
   */
  public void selectWaypoint(Waypoint waypoint, boolean toggle) {

    if (selectedWaypointProp.getValue() == waypoint && toggle) {
      selectedWaypointProp.getValue().getIcon().pseudoClassStateChanged(selected, false);
      drawPane.requestFocus();
      selectedWaypointProp.setValue(null);
      currentPath.set(null);
    } else {
      if (selectedWaypointProp.getValue() != null) {
        selectedWaypointProp.getValue().getIcon().pseudoClassStateChanged(selected, false);
      }
      selectedWaypointProp.setValue(waypoint);
      waypoint.getIcon().pseudoClassStateChanged(selected, true);
      waypoint.getIcon().requestFocus();
      waypoint.getIcon().toFront();
      currentPath.set(selectedWaypointProp.getValue().getPath());
    }
  }

  private void setupPress() {
    drawPane.setOnMouseClicked(e -> {
      if (selectedWaypointProp.getValue() != null) {
        selectedWaypointProp.getValue().getIcon().pseudoClassStateChanged(selected, false);
        selectedWaypointProp.setValue(null);
      }
    });
  }


  /**
   * Flip the current path.
   *
   * @param horizontal True if horizontal flip, false if vertical
   */
  public void flip(boolean horizontal) {
    currentPath.get().flip(horizontal, drawPane);
  }

  public void setPathDirectory(String pathDirectory) {
    this.pathDirectory = pathDirectory;
  }

  public String getPathDirectory() {
    return pathDirectory;
  }


  public ObjectProperty<Path> currentPathProperty() {
    return currentPath;
  }

  /**
   * Duplicates the selected path.
   * @param pathDirectory The directory to save the new path to.
   * @return The new path.
   */
  public Path duplicate(String pathDirectory) {
    Path path = currentPath.get();
    String fileName = MainIOUtil.getValidFileName(pathDirectory, path.getPathNameNoExtension(), ".path");
    return path.duplicate(fileName);
  }

  public Property<Waypoint> getSelectedWaypointProp() {
    return selectedWaypointProp;
  }

  public boolean checkBounds(Waypoint obj) {
    return drawPane.getLayoutBounds().contains(obj.getCoords())
            && drawPane.getLayoutBounds().contains(obj.getCoords().add(obj.getTangent()));
  }
}
