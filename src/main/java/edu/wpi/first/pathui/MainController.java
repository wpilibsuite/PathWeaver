package edu.wpi.first.pathui;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
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
  @SuppressWarnings("PMD.NcssCount") // will be refactored later; the complex code is for the demo only
  private void initialize() {
    stack.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    backgroundImage.setImage(new Image("edu/wpi/first/pathui/2018-field.jpg"));

    setupDrag();

    createInitialWaypoints();
    drawPane.setOnMousePressed(e -> {
      if (selectedWaypoint != null) {
        selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
        selectedWaypoint = null;
      }
    });
  }

  private void createInitialWaypoints() {
    Waypoint start = new Waypoint(100, 100, false);
    start.setTheta(0);
    Waypoint end = new Waypoint(500, 500, false);
    end.setTheta(3.14 / 2);
    drawPane.getChildren().add(start.getTangentLine());
    drawPane.getChildren().add(end.getTangentLine());
    drawPane.getChildren().add(start.getDot());
    drawPane.getChildren().add(end.getDot());
    start.setNextWaypoint(end);
    end.setPreviousWaypoint(start);
    start.setTangent(new Point2D(200, 0));
    end.setTangent(new Point2D(0, 200));
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
      selectWaypoint(waypoint);
      e.consume();
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
    wp.setX(event.getX());
    wp.setY(event.getY());
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
    Waypoint newPoint = new Waypoint((previous.getX() + next.getX()) / 2, (previous.getY() + next.getY()) / 2, false);
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
      if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
        Waypoint previousWaypoint = newPoint.getPreviousWaypoint();
        Waypoint nextWaypoint = newPoint.getNextWaypoint();
        if (previousWaypoint != null && nextWaypoint != null) {
          drawPane.getChildren().remove(newPoint.getDot());
          drawPane.getChildren().remove(newPoint.getTangentLine());
          drawPane.getChildren().remove(newPoint.getPreviousSpline().getCubic());
          drawPane.getChildren().remove(newPoint.getNextSpline().getCubic());
          previousWaypoint.setNextWaypoint(nextWaypoint);
          nextWaypoint.setPreviousWaypoint(previousWaypoint);
          createCurve(previousWaypoint, nextWaypoint);
          previousWaypoint.update();
          nextWaypoint.update();
        }
      }
    });
  }


}
