package edu.wpi.first.pathweaver.path;

import com.sun.javafx.collections.ObservableListWrapper;
import edu.wpi.first.pathweaver.Field;
import edu.wpi.first.pathweaver.ProjectPreferences;
import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.global.CurrentSelections;
import edu.wpi.first.pathweaver.spline.Spline;
import edu.wpi.first.pathweaver.spline.SplineFactory;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class Path {
    protected static final PseudoClass SELECTED_CLASS = PseudoClass.getPseudoClass("selected");
    protected static final double DEFAULT_SPLINE_SCALE = 6; // NOPMD
    protected static final double DEFAULT_CIRCLE_SCALE = .75; // NOPMD
    protected static final double DEFAULT_LINE_SCALE = 4; // NOPMD

    protected final Field field = ProjectPreferences.getInstance().getField();
    protected final ObservableList<Waypoint> waypoints = new ObservableListWrapper<>(new ArrayList<>());
    protected Group mainGroup = new Group();

    protected final Spline spline;
    protected final String pathName;
    protected int subchildIdx = 0;

    public Path(SplineFactory splineFactory, String pathName) {
        this.spline = splineFactory.makeSpline(waypoints, this);
        this.pathName = Objects.requireNonNull(pathName);
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public abstract Path duplicate(String pathName);

    /**
     * Converts the unit system of a this Path.
     *
     * @param from Unit to convert from.
     * @param to   Unit to convert to.
     */
    public final void convertUnit(Unit<Length> from, Unit<Length> to) {
        for (Waypoint wp : waypoints) {
            wp.convertUnit(from, to);
        }
    }

    public void update() {
        spline.update();
    }

    public final Waypoint getStart() {
        return waypoints.get(0);
    }

    public final Waypoint getEnd() {
        return waypoints.get(waypoints.size() - 1);
    }

    public final Spline getSpline() {
        return spline;
    }

    public final Group getMainGroup() {
        return mainGroup;
    }

    public final String getPathName() {
        return pathName;
    }

    /**
     * Removes extension and version number from filename.
     *
     * @return Filename without ".path" and version number.
     */
    public String getPathNameNoExtension() {
        String extension = ".path";
        String filename = pathName;
        if (pathName.endsWith(extension)) {
            filename = filename.substring(0, filename.length() - extension.length());
        }
        return filename;
    }

    public Waypoint addWaypoint(Point2D coordinates, Waypoint start, Waypoint end) {
        for (int i = 1; i < waypoints.size(); i++) {
            if (waypoints.get(i - 1) == start && waypoints.get(i) == end) {
                Waypoint toAdd = new Waypoint(coordinates, new Point2D(0, 0), false);
                waypoints.add(i, toAdd);

                updateTheta(toAdd);
                return toAdd;
            }
        }
        throw new AssertionError(
                "Endpoints provided not are invalid segment");
    }

    public void recalculateTangents(Waypoint wp) {
        int curWpIndex = getWaypoints().indexOf(wp);
        Waypoint previous = getWaypoints().get(curWpIndex - 1);
        Waypoint next = getWaypoints().get(curWpIndex + 1);

        updateTheta(wp);
        updateTheta(previous);
        updateTheta(next);

        update();

    }

    /**
     * Forces recomputation of optimal theta value.
     *
     * @param wp Waypoint to update theta for.
     */
    @SuppressWarnings("PMD.NcssCount")
    protected void updateTheta(Waypoint wp) {
        if(wp.isLockTangent()) {
            return;
        }

        int curWpIndex = getWaypoints().indexOf(wp);
        Waypoint previous = getWaypoints().get(curWpIndex - 1);
        Waypoint next = getWaypoints().get(curWpIndex + 1);

        Point2D p1 = previous.getCoords();
        Point2D p2 = wp.getCoords();
        Point2D p3 = next.getCoords();

        Point2D p1Scaled = new Point2D(0, 0);
        Point2D p2Scaled = p2.subtract(p1).multiply(1 / p3.distance(p1));
        Point2D p3Shifted = p3.subtract(p1);
        Point2D p3Scaled = p3Shifted.multiply(1 / p3.distance(p1)); // scale

        //refactor later
        // Point2D q = new Point2D(0, 0); // for reference
        Point2D r = new Point2D(p2Scaled.getX() * p3Scaled.getX() + p2Scaled.getY() * p3Scaled.getY(),
                -p2Scaled.getX() * p3Scaled.getY() + p2Scaled.getY() * p3Scaled.getX());
        // Point2D s = new Point2D(1, 0); // for reference

        double beta = 1 - 2 * r.getX();
        double gamma = Math.pow(4 * (r.getX() - Math.pow(r.distance(p1Scaled), 2)) - 3, 3) / 27;
        double lambda = Math.pow(-gamma, 1.0 / 6.0);

        double sqrtGamma = Math.sqrt(-gamma - Math.pow(beta, 2));
        double phi1 = Math.atan2(sqrtGamma, beta) / 3;
        double ur = lambda * Math.cos(phi1);
        double ui = lambda * Math.sin(phi1);
        double phi2 = Math.atan2(-sqrtGamma, beta) / 3;

        double zr = lambda * Math.cos(phi2);
        double zi = lambda * Math.sin(phi2);

        double t1 = 1.0 / 2 + ur + zr / 2;
        double t2 = 1.0 / 2 - (1.0 / 4) * (ur + zr + Math.sqrt(3) * (ui - zi));
        double t3 = 1.0 / 2 - (1.0 / 4) * (ur + zr - Math.sqrt(3) * (ui - zi));

        double t;
        if (t1 > 0 && t1 < 1) {
            t = t1;
        } else if (t2 > 0 && t2 < 1) {
            t = t2;
        } else {
            t = t3;
        }

        Point2D a1 = p2.subtract(p1).subtract(p3Shifted.multiply(t)).multiply(1 / (t * t - t));
        Point2D a2 = p3Shifted.subtract(a1);

        Point2D tangent = a1.multiply(2 * t).add(a2).multiply(1. / 3);

        wp.setTangent(tangent);
    }

    public void enableSubchildSelector(int i) {
        this.subchildIdx = i;
        for (Waypoint wp : waypoints) {
            wp.enableSubchildSelector(subchildIdx);
        }
        spline.enableSubchildSelector(subchildIdx);
    }

    public void toggleWaypoint(Waypoint waypoint) {
        if (CurrentSelections.getCurWaypoint() == waypoint) {
            deselectWaypoint(waypoint);
        } else {
            selectWaypoint(waypoint);
        }
    }

    public void selectWaypoint(Waypoint waypoint) {
        Waypoint curWaypoint = CurrentSelections.getCurWaypoint();
        if (curWaypoint != null) {
            deselectWaypoint(curWaypoint);
        }
        waypoint.getIcon().pseudoClassStateChanged(SELECTED_CLASS, true);
        waypoint.getIcon().requestFocus();
        waypoint.getIcon().toFront();
        CurrentSelections.setCurWaypoint(waypoint);
        CurrentSelections.setCurPath(this);
    }

    public void deselectWaypoint(Waypoint waypoint) {
        Waypoint curWaypoint = CurrentSelections.getCurWaypoint();
        if (CurrentSelections.getCurWaypoint() == waypoint) {
            curWaypoint.getIcon().pseudoClassStateChanged(SELECTED_CLASS, false);
            mainGroup.requestFocus();
            CurrentSelections.setCurWaypoint(null);
        }
    }

    public boolean removeWaypoint(Waypoint waypoint) {
        if (waypoints.size() > 2) {
            waypoints.remove(waypoint);
            return true;
        }
        return false;
    }

    /**
     * Reflects the Path across an axis.
     * The coordinate system's origin is the starting point of the Path.
     *
     * @param horizontal Flip over horizontal axis?
     * @param drawPane   Pane to check validity of new points.
     */
    public void flip(boolean horizontal, Pane drawPane) {
        // Check if any new points are outside drawPane
        for (Waypoint wp : waypoints) {
            if (!drawPane.contains(reflectPoint(getStart(), wp, horizontal, false))) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("");
                a.setHeaderText("The path could not be flipped.");
                a.setContentText("Flipping this path would cause it to go out of bounds");
                a.showAndWait();
                return; // The new path is invalid
            }
        }
        // New waypoints are valid, update all Waypoints
        for (Waypoint wp : waypoints) {
            Point2D reflectedPos = reflectPoint(getStart(), wp, horizontal, false);
            Point2D reflectedTangent = reflectPoint(getStart(), wp, horizontal, true);
            wp.setX(reflectedPos.getX());
            wp.setY(reflectedPos.getY());
            wp.setTangent(reflectedTangent);
        }
    }

    private Point2D reflectPoint(Waypoint start, Waypoint point, boolean horizontal, boolean tangent) {
        Point2D coords;
        Point2D minus;
        if (tangent) {
            coords = point.getTangent();
            if (horizontal) {
                minus = new Point2D(coords.getX() * 2.0, 0.0);
            } else {
                minus = new Point2D(0.0, coords.getY() * 2.0);
            }
            return coords.subtract(minus);
        } else {
            coords = point.getCoords();
            if (horizontal) {
                minus = new Point2D(point.relativeTo(start).getX() * 2.0, 0.0);
            } else {
                minus = new Point2D(0.0, point.relativeTo(start).getY() * 2.0);
            }
            return coords.subtract(minus);
        }
    }
}
