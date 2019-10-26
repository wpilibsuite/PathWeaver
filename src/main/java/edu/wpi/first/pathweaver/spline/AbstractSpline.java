package edu.wpi.first.pathweaver.spline;

import edu.wpi.first.pathweaver.DataFormats;
import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.global.CurrentSelections;
import javafx.scene.Group;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.List;
import java.util.Map;

/**
 * This class provides a partial implementation of the Spline interface,
 * and also sets up drag and drop for this Spline.
 *
 * Note: This abstract class places drag and drop constraints on implementors. Implementors will need
 * to make sure {@link CurrentSelections} endpoints get updated for dragging in the middle of a Path to work.
 *
 * @see CurrentSelections

 */
public abstract class AbstractSpline implements Spline {
    protected final Group group = new Group();
    protected final List<Waypoint> waypoints;

    protected AbstractSpline(List<Waypoint> waypoints) {
        this.waypoints = waypoints;

        group.setOnDragDetected(event -> {
            Dragboard board = group.startDragAndDrop(TransferMode.ANY);
            board.setContent(Map.of(DataFormats.SPLINE, "Spline"));

            event.consume();
        });
    }
}
