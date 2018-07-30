package edu.wpi.first.pathweaver;

import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class DragHandler {

  private final PathDisplayController controller;
  private final Pane drawPane;
  public static Spline currentSpline = new NullSpline();

  private boolean isShiftDown = false;
  private boolean splineDragStarted = false;

  /**
   * Creates the DragHandler, which sets up and manages all drag interactions for the given PathDisplayController.
   *
   * @param parent   The PathDisplayController that this DragHandler manages
   * @param drawPane The PathDisplayController's Pane.
   */
  public DragHandler(PathDisplayController parent, Pane drawPane) {
    this.controller = parent;
    this.drawPane = drawPane;
    this.setupDrag();
  }

  private void finishDrag() {
    PathIOUtil.export(controller.getPathDirectory(), Waypoint.currentWaypoint.getPath());
    splineDragStarted = false;
  }

  private void handleDrag(DragEvent event) {
    Dragboard dragboard = event.getDragboard();
    Waypoint wp = Waypoint.currentWaypoint;
    event.acceptTransferModes(TransferMode.MOVE);
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
  }

  private void setupDrag() {
    drawPane.setOnDragDone(event -> finishDrag());
    drawPane.setOnDragOver(this::handleDrag);
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
    wp.getPath().updateSplines();
  }

  private void handleSplineDrag(DragEvent event, Waypoint wp) {
    if (splineDragStarted) {
      handleWaypointDrag(event, wp);
    } else {
      Spline current = currentSpline;
      current.addPathWaypoint(controller);
      current = new NullSpline();
      splineDragStarted = true;
    }
  }

  private void handlePathMoveDrag(DragEvent event, Waypoint wp) {
    double offsetX = event.getX() - wp.getX();
    double offsetY = event.getY() - wp.getY();

    // Make sure all waypoints will be within the bounds
    for (Waypoint point : wp.getPath().getWaypoints()) {
      double wpNewX = point.getX() + offsetX;
      double wpNewY = point.getY() + offsetY;
      if (!checkBounds(wpNewX, wpNewY)) {
        return;
      }
    }

    // Apply new positions
    for (Waypoint point : wp.getPath().getWaypoints()) {
      double wpNewX = point.getX() + offsetX;
      double wpNewY = point.getY() + offsetY;
      point.setX(wpNewX);
      point.setY(wpNewY);
    }
  }
}
