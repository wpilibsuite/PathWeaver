package edu.wpi.first.pathui;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MainController {
  @FXML private ImageView backgroundImage;
  @FXML private StackPane stack;
  @FXML private Pane drawPane;
  private Waypoint selectedWaypoint = null;
  private final PseudoClass selected = PseudoClass.getPseudoClass("selected");

  @FXML
  private void initialize() {
    stack.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    Image image = new Image("edu/wpi/first/pathui/2018-field.jpg");
    backgroundImage.setImage(image);
    final double aspectRatio = image.getWidth() / image.getHeight();

    drawPane.maxWidthProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(backgroundImage.getFitWidth(), backgroundImage.getFitHeight() * aspectRatio),
        backgroundImage.fitWidthProperty(), backgroundImage.fitHeightProperty()));
    drawPane.maxHeightProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(backgroundImage.getFitHeight(), backgroundImage.getFitWidth() / aspectRatio),
        backgroundImage.fitWidthProperty(), backgroundImage.fitHeightProperty()));
    drawPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(backgroundImage.getFitWidth(), backgroundImage.getFitHeight() * aspectRatio),
        backgroundImage.fitWidthProperty(), backgroundImage.fitHeightProperty()));
    drawPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(backgroundImage.getFitHeight(), backgroundImage.getFitWidth() / aspectRatio),
        backgroundImage.fitWidthProperty(), backgroundImage.fitHeightProperty()));


    setupDrag();

    Waypoint.sceneHeightProperty().bind(drawPane.heightProperty());
    Waypoint.sceneWidthProperty().bind(drawPane.widthProperty());

    createInitialWaypoints();
    drawPane.setOnMousePressed(e -> {
      if (selectedWaypoint != null) {
        selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
        selectedWaypoint = null;
      }
    });
  }

  private void createInitialWaypoints() {
    Waypoint start = new Waypoint(0.1, 0.1, false);
    Waypoint end = new Waypoint(0.9, 0.9, false);
    drawPane.getChildren().add(start.getTangentLine());
    drawPane.getChildren().add(end.getTangentLine());
    drawPane.getChildren().add(start.getDot());
    drawPane.getChildren().add(end.getDot());
    start.setNextWaypoint(end);
    end.setPreviousWaypoint(start);
    start.setTangent(new Point2D(0.3, 0));
    end.setTangent(new Point2D(0, 0.3));
    createCurve(start, end);
    setupWaypoint(start);
    setupWaypoint(end);
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

  private void setupDrag() {
    drawPane.setOnDragDone(event -> {
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

  private void handleWaypointDrag(DragEvent event, Waypoint wp) {
    wp.setTrueX(event.getX() / Waypoint.getSceneWidth()); //use dimensions inside Waypoint for consistency
    wp.setTrueY(event.getY() / Waypoint.getSceneHeight());
  }

  private void handleVectorDrag(DragEvent event, Waypoint wp) {
    Point2D pt = new Point2D(event.getX(), event.getY());
    Point2D vector = pt.subtract(wp.getxPixel(), wp.getyPixel());

    Point2D unitLessVector = new Point2D(vector.getX() / Waypoint.getSceneWidth(),
        vector.getY() / Waypoint.getSceneHeight());

    wp.setTangent(unitLessVector);
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
      Waypoint newPoint = addNewWaypoint(current.getStart(), current.getEnd());
      Spline.currentSpline = null;
      Waypoint.currentWaypoint = newPoint;
    } else {
      handleWaypointDrag(event, wp);
    }
  }


  private void createCurve(Waypoint start, Waypoint end) {
    Spline curve = new Spline(start, end);
    drawPane.getChildren().add(curve.getCubic());
    curve.getCubic().toBack();
  }

  private Waypoint addNewWaypoint(Waypoint previous, Waypoint next) {
    if (previous.getNextWaypoint() != next || next.getPreviousWaypoint() != previous) {
      throw new IllegalArgumentException("New Waypoint not between connected points");
    }
    Waypoint newPoint = new Waypoint((previous.getxPixel() + next.getxPixel()) / 2,
        (previous.getyPixel() + next.getyPixel()) / 2, false);
    newPoint.setPreviousWaypoint(previous);
    newPoint.setNextWaypoint(next);
    next.setPreviousWaypoint(newPoint);
    previous.setNextWaypoint(newPoint);
    drawPane.getChildren().add(newPoint.getTangentLine());
    drawPane.getChildren().add(newPoint.getDot());

    //tell spline going from previous -> next to go from previous -> new
    newPoint.addSpline(previous.getNextSpline(), false);
    createCurve(newPoint, next); //new spline from new -> next

    newPoint.update();
    makeDeletable(newPoint);
    setupWaypoint(newPoint);
    selectWaypoint(newPoint);

    return newPoint;
  }

  /**
   * Deletes the point and its connected splines when DELETE or BACKSPACE is pressed, then connects the neighbors.
   *
   * @param newPoint the point to make deletable
   */
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
    createCurve(previousWaypoint, nextWaypoint);
    previousWaypoint.update();
    nextWaypoint.update();
  }

}
