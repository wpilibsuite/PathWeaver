package edu.wpi.first.pathweaver.global;

import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.path.Path;
import javafx.beans.property.SimpleObjectProperty;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class CurrentSelections {
    private static SimpleObjectProperty<Waypoint> curSplineStart = new SimpleObjectProperty<>();
    private static SimpleObjectProperty<Waypoint> curSplineEnd = new SimpleObjectProperty<>();
    private static SimpleObjectProperty<Waypoint> curWaypoint = new SimpleObjectProperty<>();
    private static SimpleObjectProperty<Path> curPath = new SimpleObjectProperty<>();

    private CurrentSelections() {
        throw new UnsupportedOperationException("This is a utility class");
    }

    public static Waypoint getCurSplineStart() {
        return curSplineStart.get();
    }

    public static SimpleObjectProperty<Waypoint> curSplineStartProperty() {
        return curSplineStart;
    }

    public static void setCurSplineStart(Waypoint curSplineStart) {
        CurrentSelections.curSplineStart.set(curSplineStart);
    }

    public static Waypoint getCurSplineEnd() {
        return curSplineEnd.get();
    }

    public static SimpleObjectProperty<Waypoint> curSplineEndProperty() {
        return curSplineEnd;
    }

    public static void setCurSplineEnd(Waypoint curSplineEnd) {
        CurrentSelections.curSplineEnd.set(curSplineEnd);
    }

    public static Waypoint getCurWaypoint() {
        return curWaypoint.get();
    }

    public static SimpleObjectProperty<Waypoint> curWaypointProperty() {
        return curWaypoint;
    }

    public static void setCurWaypoint(Waypoint curWaypoint) {
        CurrentSelections.curWaypoint.set(curWaypoint);
    }

    public static Path getCurPath() {
        return curPath.get();
    }

    public static SimpleObjectProperty<Path> curPathProperty() {
        return curPath;
    }

    public static void setCurPath(Path curPath) {
        CurrentSelections.curPath.set(curPath);
    }
}
