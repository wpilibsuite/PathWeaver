package edu.wpi.first.pathweaver;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

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

  private final ObjectProperty<Waypoint> selectedWaypoint = new SimpleObjectProperty<>();

  private final ObjectProperty<Path> currentPath = new SimpleObjectProperty<>();
  private Field field;

  private final ObservableList<Path> pathList = FXCollections.observableArrayList();
  private String pathDirectory;

  private final double circleScale = .75; //NOPMD should be static, will be modified later
  private final double splineScale = 6; //NOPMD should be static, will be modified later
  private final double lineScale = 4; //NOPMD should be static, will be modified later

  @FXML
  private Group splineGroup;
  @FXML
  private Group waypointGroup;
  @FXML
  private Group vectorGroup;

  @FXML
  private void initialize() {
    field = ProjectPreferences.getInstance().getField();
    Image image = field.getImage();
    backgroundImage.setImage(image);
    topPane.getStyleClass().add("pane");
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
      selectedWaypoint.setValue(null);
      if (newValue == null) {
        return;
      }
      vectorGroup.getChildren().addAll(newValue.getTangentLines());
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
      SaveManager.getInstance().saveChange(newPath);
    }
    pathList.add(newPath);
    return newPath;
  }

  /**
   * Remove all paths from Controller.
   */
  public void removeAllPath() {
    pathList.clear();
  }


  //between this and above public function better names could be found
  private void addPathToPane(Path newPath) {
    for (Waypoint wp : newPath.getWaypoints()) {
      setupWaypoint(wp);
      addWaypointToPane(wp);
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
    current.getSpline().addToGroup(splineGroup, splineScale / field.getScale());
  }


  private void removePathFromPane(Path newPath) {
    for (Waypoint wp : newPath.getWaypoints()) {
      wp.getSpline().removeFromGroup(splineGroup);
    }
    for (Waypoint wp : newPath.getWaypoints()) {
      waypointGroup.getChildren().remove(wp.getIcon());
      vectorGroup.getChildren().remove(wp.getTangentLine());
    }
  }

  private void setupDrawPaneSizing(Image image) {
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
        menu.getItems().add(FxUtils.menuItem("Delete", event -> delete(waypoint)));
      }
      if (waypoint.getTangentLine().isVisible()) {
        menu.getItems().add(FxUtils.menuItem("Hide control vector",
            event -> waypoint.getTangentLine().setVisible(false)));
      } else {
        menu.getItems().add(FxUtils.menuItem("Show control vector",
            event -> waypoint.getTangentLine().setVisible(true)));
      }
      menu.show(drawPane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
  }

  @FXML
  private void keyPressed(KeyEvent event) {
    KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    if ((event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)
        && isDeletable(getSelectedWaypoint())) {
      delete(getSelectedWaypoint());
    } else if (save.match(event) && currentPath.get() != null) {
      SaveManager.getInstance().saveChange(currentPath.get());
    }
  }


  private boolean isDeletable(Waypoint waypoint) {
    return waypoint.getPath().getStart() != waypoint
        && waypoint.getPath().getEnd() != waypoint;
  }

  private void delete(Waypoint waypoint) {
    selectedWaypoint.setValue(null);
    Path path = waypoint.getPath();
    Waypoint previous = path.getWaypoints().get(path.getWaypoints().indexOf(waypoint) - 1);
    waypointGroup.getChildren().remove(waypoint.getIcon());
    vectorGroup.getChildren().remove(waypoint.getTangentLine());
    waypoint.getSpline().removeFromGroup(splineGroup);
    path.remove(waypoint);
    previous.update();
    Waypoint next = path.getWaypoints().get(path.getWaypoints().indexOf(waypoint) + 1);
    next.update();
    path.swapToPathfinderSplines();
    SaveManager.getInstance().addChange(path);
  }

  /**
   * Selects or deselects a waypoint and associated path for the purposes of drawing, dragging, and otherwise modifying
   * If toggle is true, then deselect the waypoint if it is the same as the currently selected waypoint.
   *
   * @param waypoint The waypoint to be selected
   * @param toggle   Whether to toggle the selection if possible
   */
  public void selectWaypoint(Waypoint waypoint, boolean toggle) {

    if (getSelectedWaypoint() == waypoint && toggle) {
      getSelectedWaypoint().getIcon().pseudoClassStateChanged(selected, false);
      drawPane.requestFocus();
      selectedWaypoint.setValue(null);
      currentPath.set(null);
    } else {
      if (getSelectedWaypoint() != null) {
        getSelectedWaypoint().getIcon().pseudoClassStateChanged(selected, false);
      }
      selectedWaypoint.setValue(waypoint);
      waypoint.getIcon().pseudoClassStateChanged(selected, true);
      waypoint.getIcon().requestFocus();
      waypoint.getIcon().toFront();
      currentPath.set(getSelectedWaypoint().getPath());
    }
  }

  private void setupPress() {
    drawPane.setOnMouseClicked(e -> {
      if (getSelectedWaypoint() != null) {
        getSelectedWaypoint().getIcon().pseudoClassStateChanged(selected, false);
        selectedWaypoint.setValue(null);
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
    SaveManager.getInstance().addChange(currentPath.get());
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

  public Waypoint getSelectedWaypoint() {
    return selectedWaypoint.getValue();
  }

  public ObservableValue<Waypoint> selectedWaypointProperty() {
    return selectedWaypoint;
  }

  /**
   * Checks if the given x, y coordinates are within the valid area of the drawpane.
   * @param x X coordinate
   * @param y Y coordinate
   * @return True if X, Y is within the bounds of the drawpane.
   */
  public boolean checkBounds(double x, double y) {
    return drawPane.getLayoutBounds().contains(x, y);
  }

}
