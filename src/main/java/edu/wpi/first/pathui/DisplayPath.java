package edu.wpi.first.pathui;

import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;

public class DisplayPath {
  private Pane pathPane = new Pane();
  static private Waypoint selectedWaypoint = null;
  private final PseudoClass selected = PseudoClass.getPseudoClass("selected");
  public DisplayPath(){
    createInitialWaypoints();
    setupDrag();
    pathPane.setPickOnBounds(false);
  }
  private void createInitialWaypoints() {
    Waypoint start = new Waypoint(0, 0, false);
    start.setTheta(0);
    Waypoint end = new Waypoint(250, 250, false);
    end.setTheta(3.14 / 2);
    pathPane.getChildren().add(start.getTangentLine());
    pathPane.getChildren().add(end.getTangentLine());
    pathPane.getChildren().add(start.getDot());
    pathPane.getChildren().add(end.getDot());
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
      pathPane.requestFocus();
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
      menu.show(pathPane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
  }

  private void setupDrag() {
    pathPane.setOnDragDone(event -> {
      Waypoint.currentWaypoint = null;
      Spline.currentSpline = null;
    });
    pathPane.setOnDragOver(event -> {
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
    pathPane.setOnMousePressed(e -> {
      if (selectedWaypoint != null) {
        selectedWaypoint.getDot().pseudoClassStateChanged(selected, false);
        selectedWaypoint = null;
      }
    });
  }

  private void handleWaypointDrag(DragEvent event, Waypoint wp) {
    if(pathPane.getLayoutBounds().contains(event.getX(),event.getY())){
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
      Waypoint newPoint = addNewWaypoint(current.getStart(), current.getEnd());
      Spline.currentSpline = null;
      Waypoint.currentWaypoint = newPoint;
    } else {
      handleWaypointDrag(event, wp);
    }
  }


  private void createCurve(Waypoint start, Waypoint end) {
    Spline curve = new Spline(start, end);
    pathPane.getChildren().add(curve.getCubic());
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
    pathPane.getChildren().add(newPoint.getTangentLine());
    pathPane.getChildren().add(newPoint.getDot());

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
    pathPane.getChildren().remove(waypoint.getDot());
    pathPane.getChildren().remove(waypoint.getTangentLine());
    pathPane.getChildren().remove(waypoint.getPreviousSpline().getCubic());
    pathPane.getChildren().remove(waypoint.getNextSpline().getCubic());
    previousWaypoint.setNextWaypoint(nextWaypoint);
    nextWaypoint.setPreviousWaypoint(previousWaypoint);
    createCurve(previousWaypoint, nextWaypoint);
    previousWaypoint.update();
    nextWaypoint.update();
  }

  public Pane getPathPane() {
    return pathPane;
  }
}
