package edu.wpi.first.pathui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.util.Pair;

public class PathDisplayController {
  @FXML private ImageView backgroundImage;
  @FXML private Pane drawPane;
  @FXML private Group group;
  @FXML private Pane topPane;
  private final PseudoClass selected = PseudoClass.getPseudoClass("selected");
  private Waypoint selectedWaypoint = null;
  private Image image;

  private final ObservableList<Path> pathList = FXCollections.observableArrayList();
  private String pathDirectory;

  @FXML
  private void initialize() {

    image = new Image("edu/wpi/first/pathui/2018-field.jpg");
    backgroundImage.setImage(image);
    Scale scale = new Scale();
    scale.xProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
        topPane.widthProperty(), topPane.heightProperty()));
    scale.yProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth() / image.getWidth(), topPane.getHeight() / image.getHeight()),
        topPane.widthProperty(), topPane.heightProperty()));

    group.getTransforms().add(scale);
    setupDrawPaneSizing();
    setupDrag();
    setupPress();
    setupPathList();
  }

  private void setupPathList() {
    pathList.addListener((ListChangeListener) change -> {
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
  }

  /**
   * Add path to Controller.
   *
   * @param fileLocations Current working directory
   * @param fileName      Name of path file inside directory
   */
  public void addPath(String fileLocations, String fileName) {
    for (Path path : pathList) {
      if (fileName == path.getPathName()) {
        return;
      }
    }
    Path newPath = PathIOUtil.importPath(fileLocations, fileName);
    pathList.add(newPath);
  }

  /**
   * Remove all paths from Controller.
   */
  public void removeAllPath() {
    pathList.clear();
  }


  //between this and above public function better names could be found
  private void addPathToPane(Path newPath) {
    Waypoint current = newPath.getStart();
    while (current != null) {
      setupWaypoint(current);
      drawPane.getChildren().add(current.getDot());
      drawPane.getChildren().add(current.getTangentLine());
      current.getTangentLine().toBack();
      current = current.getNextWaypoint();
      if (current != null) {
        drawPane.getChildren().add(current.getPreviousSpline().getCubic());
        current.getPreviousSpline().getCubic().toBack();
      }
    }
  }

  private void removePathFromPane(Path newPath) {
    Waypoint current = newPath.getStart();
    while (current != null) {
      drawPane.getChildren().remove(current.getDot());
      drawPane.getChildren().remove(current.getTangentLine());
      current = current.getNextWaypoint();
      if (current != null) {
        drawPane.getChildren().remove(current.getPreviousSpline().getCubic());
      }
    }
  }


  private void setupDrawPaneSizing() {

    drawPane.setMaxWidth(image.getWidth());
    drawPane.setMinWidth(image.getWidth());
    drawPane.setMaxHeight(image.getHeight());
    drawPane.setMinHeight(image.getHeight());
    drawPane.setPrefHeight(image.getHeight());
    drawPane.setPrefWidth(image.getWidth());

  }

  private void setupWaypoint(Waypoint waypoint) {
    waypoint.getDot().setOnMousePressed(e -> {
      if (e.getClickCount() == 1 && e.getButton() == MouseButton.PRIMARY) {
        selectWaypoint(waypoint);
        e.consume();
      }
    });
    waypoint.getDot().setOnContextMenuRequested(e -> {
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

  private void makeDeletable(Waypoint newPoint) {
    newPoint.getDot().setOnKeyPressed(event -> {
      if ((event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) && isDeletable(newPoint)) {
        delete(newPoint);
      }
    });
  }

  private boolean isDeletable(Waypoint waypoint) {
    return waypoint.getPreviousWaypoint() != null
        && waypoint.getNextWaypoint() != null;
  }

  private void delete(Waypoint waypoint) {
    Waypoint previousWaypoint = waypoint.getPreviousWaypoint();
    Waypoint nextWaypoint = waypoint.getNextWaypoint();
    drawPane.getChildren().remove(waypoint.getDot());
    drawPane.getChildren().remove(waypoint.getTangentLine());
    drawPane.getChildren().remove(waypoint.getPreviousSpline().getCubic());
    drawPane.getChildren().remove(waypoint.getNextSpline().getCubic());
    previousWaypoint.setNextWaypoint(nextWaypoint);
    nextWaypoint.setPreviousWaypoint(previousWaypoint);
    Spline newCurve = previousWaypoint.getPath().createCurve(previousWaypoint, nextWaypoint);
    drawPane.getChildren().add(newCurve.getCubic());
    newCurve.getCubic().toBack();
    previousWaypoint.update();
    nextWaypoint.update();
  }

  private void selectWaypoint(Waypoint waypoint) {
    if (selectedWaypoint == waypoint) {
      selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
      drawPane.requestFocus();
      selectedWaypoint = null;
    } else {
      if (selectedWaypoint != null) {
        selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
      }
      selectedWaypoint = waypoint;
      waypoint.getDot().pseudoClassStateChanged(selected, true);
      waypoint.getDot().requestFocus();
    }
  }

  private void setupDrag() {
    drawPane.setOnDragDone(event -> {
      PathIOUtil.export(pathDirectory, Waypoint.currentWaypoint.getPath());
      Waypoint.currentWaypoint = null;
      Spline.currentSpline = null;
    });
    drawPane.setOnDragOver(event -> {
      Dragboard dragboard = event.getDragboard();
      Waypoint wp = Waypoint.currentWaypoint;
      if (dragboard.hasContent(DataFormats.WAYPOINT)) {
        handleWaypointDrag(event, wp);
      } else if (dragboard.hasContent(DataFormats.CONTROL_VECTOR)) {
        handleVectorDrag(event, wp);
      } else if (dragboard.hasContent(DataFormats.SPLINE)) {
        handleSplineDrag(event, wp);
      }
    });
  }

  private void setupPress() {
    drawPane.setOnMousePressed(e -> {
      if (selectedWaypoint != null) {
        selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
        selectedWaypoint = null;
      }
    });
  }

  private void handleWaypointDrag(DragEvent event, Waypoint wp) {
    if (drawPane.getLayoutBounds().contains(event.getX(), event.getY())) {
      wp.setX(event.getX());
      wp.setY(event.getY());
    }
  }

  private void handleVectorDrag(DragEvent event, Waypoint wp) {
    Point2D pt = new Point2D(event.getX(), event.getY());
    wp.setTangent(pt.subtract(wp.getX(), wp.getY()));
    wp.lockTangent();
    if (wp.getPreviousSpline() != null) {
      wp.getPreviousSpline().updateControlPoints();
    }
    if (wp.getNextSpline() != null) {
      wp.getNextSpline().updateControlPoints();
    }
  }

  private void handleSplineDrag(DragEvent event, Waypoint wp) {
    if (Waypoint.currentWaypoint == null) {
      Spline current = Spline.currentSpline;
      Waypoint start = current.getStart();
      Waypoint end = current.getEnd();
      Waypoint newPoint = current.getEnd().getPath().addNewWaypoint(start, end);
      drawPane.getChildren().add(newPoint.getPreviousSpline().getCubic());
      drawPane.getChildren().add(newPoint.getDot());
      drawPane.getChildren().add(newPoint.getTangentLine());
      newPoint.getTangentLine().toBack();
      newPoint.getPreviousSpline().getCubic().toBack();
      makeDeletable(newPoint);
      setupWaypoint(newPoint);
      selectWaypoint(newPoint);
      Spline.currentSpline = null;
      Waypoint.currentWaypoint = newPoint;
    } else {
      handleWaypointDrag(event, wp);
    }
  }

  public void setPathDirectory(String pathDirectory) {
    this.pathDirectory = pathDirectory;
  }
}
