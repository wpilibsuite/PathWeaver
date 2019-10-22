package edu.wpi.first.pathweaver.spline;

import edu.wpi.first.pathweaver.DataFormats;
import edu.wpi.first.pathweaver.Waypoint;
import javafx.scene.Group;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.List;
import java.util.Map;

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
