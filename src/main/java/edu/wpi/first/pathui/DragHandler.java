package edu.wpi.first.pathui;

import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.Pane;

public class DragHandler {

  private final PathDisplayController controller;
  private final Pane drawPane;

  private boolean isShiftDown = false;

  /**
   * Creates the DragHandler, which sets up and manages all drag interactions for the given PathDisplayController.
   * @param parent The PathDisplayController that this DragHandler manages
   * @param drawPane The PathDisplayController's Pane.
   */
  public DragHandler(PathDisplayController parent, Pane drawPane) {
    this.controller = parent;
    this.drawPane = drawPane;
    this.setupDrag();
  }

  private void finishDrag() {
    PathIOUtil.export(controller.getPathDirectory(), Waypoint.currentWaypoint.getPath());
    Waypoint.currentWaypoint = null;
    Spline.currentSpline = null;
  }

  private void setupDrag() {
    drawPane.setOnDragDone(event -> finishDrag());
    drawPane.setOnDragOver(event -> {
      Dragboard dragboard = event.getDragboard();
      Waypoint wp = Waypoint.currentWaypoint;
      if (dragboard.hasContent(DataFormats.WAYPOINT)) {
        if (isShiftDown) {
          handlePathMoveDrag(event, wp);
        } else {
          handleWaypointDrag(event, wp);
        }
      } else if (dragboard.hasContent(DataFormats.CONTROL_VECTOR)) {
        handleVectorDrag(event, wp);
      } else if (dragboard.hasContent(DataFormats.SPLINE)) {
        handleSplineDrag(event, wp);
      }
      event.consume();
    });
    drawPane.setOnDragDetected(event -> {
      isShiftDown = event.isShiftDown();
    });
  }

  private boolean checkBounds(double x, double y) {
    return drawPane.getLayoutBounds().contains(x, y);
  }


  private void handleWaypointDrag(DragEvent event, Waypoint wp) {
    if (checkBounds(event.getX(), event.getY())) {
      wp.setX(event.getX());
      wp.setY(event.getY());
    }
    controller.selectWaypoint(wp, false);
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
      controller.addWaypointToPane(newPoint);
      newPoint.getPreviousSpline().getCubic().toBack();
      controller.setupWaypoint(newPoint);
      controller.selectWaypoint(newPoint, false);
      Spline.currentSpline = null;
      Waypoint.currentWaypoint = newPoint;
    } else {
      handleWaypointDrag(event, wp);
    }
  }

  private void handlePathMoveDrag(DragEvent event, Waypoint wp) {
    double offsetX = event.getX() - wp.getX();
    double offsetY = event.getY() - wp.getY();

    // Make sure all waypoints will be within the bounds
    Waypoint next = wp.getPath().getStart();
    while (next != null) {
      double wpNewX = next.getX() + offsetX;
      double wpNewY = next.getY() + offsetY;
      if (!checkBounds(wpNewX, wpNewY)) {
        return;
      }

      next = next.getNextWaypoint();
    }

    // Apply new positions
    next = wp.getPath().getStart();
    while (next != null) {
      double wpNewX = next.getX() + offsetX;
      double wpNewY = next.getY() + offsetY;
      next.setX(wpNewX);
      next.setY(wpNewY);

      next = next.getNextWaypoint();
    }

  }
}
