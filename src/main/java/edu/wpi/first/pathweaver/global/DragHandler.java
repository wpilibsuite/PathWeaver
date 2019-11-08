package edu.wpi.first.pathweaver.global;

import edu.wpi.first.pathweaver.DataFormats;
import edu.wpi.first.pathweaver.FieldDisplayController;
import edu.wpi.first.pathweaver.SaveManager;
import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.path.Path;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

/**
 * This class handles the drag and drop functionality for PathWeaver.
 * Implementors should be sure that the appropriate property in
 * {@link CurrentSelections} is set correctly for this functionality to work
 * properly.
 */
public class DragHandler {
	private final FieldDisplayController controller;
	private final Pane drawPane;

	private boolean isShiftDown = false;
	private boolean splineDragStarted = false;

	/**
	 * Creates the DragHandler, which sets up and manages all drag interactions for
	 * the given PathDisplayController.
	 *
	 * @param parent
	 *            The PathDisplayController that this DragHandler manages
	 * @param drawPane
	 *            The PathDisplayController's Pane.
	 */
	public DragHandler(FieldDisplayController parent, Pane drawPane) {
		this.controller = parent;
		this.drawPane = drawPane;
		this.setupDrag();
	}

	private void finishDrag() {
		SaveManager.getInstance().addChange(CurrentSelections.getCurPath());
		splineDragStarted = false;
	}

	private void handleDrag(DragEvent event) {
		Dragboard dragboard = event.getDragboard();
		Waypoint wp = CurrentSelections.getCurWaypoint();
		Path path = CurrentSelections.getCurPath();
		event.acceptTransferModes(TransferMode.MOVE);
		if (dragboard.hasContent(DataFormats.WAYPOINT)) {
			if (isShiftDown) {
				handlePathMoveDrag(event, path, wp);
			} else {
				handleWaypointDrag(event, path, wp);
			}
		} else if (dragboard.hasContent(DataFormats.CONTROL_VECTOR)) {
			handleVectorDrag(event, path, wp);
		} else if (dragboard.hasContent(DataFormats.SPLINE)) {
			handleSplineDrag(event, path, wp);
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

	private void handleWaypointDrag(DragEvent event, Path path, Waypoint point) {
		if (controller.checkBounds(event.getX(), event.getY())) {
			point.setX(event.getX());
			point.setY(event.getY());
			path.recalculateTangents(point);
			path.update();
		}
		CurrentSelections.getCurPath().selectWaypoint(point);
	}

	private void handleVectorDrag(DragEvent event, Path path, Waypoint wp) {
		Point2D pt = new Point2D(event.getX(), event.getY());
		wp.setTangent(pt.subtract(wp.getX(), wp.getY()));
		wp.lockTangentProperty().set(true);
		path.update();
	}

	private void handleSplineDrag(DragEvent event, Path path, Waypoint wp) {
		if (splineDragStarted) {
			handleWaypointDrag(event, path, wp);
		} else {
			Waypoint cur = path.addWaypoint(new Point2D(event.getX(), event.getY()),
					CurrentSelections.getCurSplineStart(), CurrentSelections.getCurSplineEnd());
			path.selectWaypoint(cur);
			CurrentSelections.setCurPath(path);
			CurrentSelections.setCurSplineStart(null);
			CurrentSelections.setCurSplineEnd(null);

			splineDragStarted = true;
		}
	}

	private void handlePathMoveDrag(DragEvent event, Path path, Waypoint point) {
		double offsetX = event.getX() - point.getX();
		double offsetY = event.getY() - point.getY();

		// Make sure all waypoints will be within the bounds
		for (Waypoint checkPoint : path.getWaypoints()) {
			double wpNewX = checkPoint.getX() + offsetX;
			double wpNewY = checkPoint.getY() + offsetY;
			if (!controller.checkBounds(wpNewX, wpNewY)) {
				return;
			}
		}

		// Apply new positions
		for (Waypoint changedPoint : path.getWaypoints()) {
			double wpNewX = changedPoint.getX() + offsetX;
			double wpNewY = changedPoint.getY() + offsetY;
			changedPoint.setX(wpNewX);
			changedPoint.setY(wpNewY);
		}

		path.update();
	}
}
