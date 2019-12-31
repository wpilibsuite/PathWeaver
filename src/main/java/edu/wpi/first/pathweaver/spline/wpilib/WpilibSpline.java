package edu.wpi.first.pathweaver.spline.wpilib;

import edu.wpi.first.pathweaver.FxUtils;
import edu.wpi.first.pathweaver.ProjectPreferences;
import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.path.Path;
import edu.wpi.first.pathweaver.spline.AbstractSpline;
import edu.wpi.first.pathweaver.spline.SplineSegment;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.spline.PoseWithCurvature;
import edu.wpi.first.wpilibj.spline.QuinticHermiteSpline;
import edu.wpi.first.wpilibj.spline.Spline;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A WpilibSpline interfaces with Wpilib to
 * calculate splines.
 */
public class WpilibSpline extends AbstractSpline {
    private static final Logger LOGGER = Logger.getLogger(WpilibSpline.class.getName());

    private final SimpleDoubleProperty strokeWidth = new SimpleDoubleProperty(1.0);
    private int subchildIdx = 0;

    private final Path path;

    @Override
    public void enableSubchildSelector(int i) {
        this.subchildIdx = i;
        for (Node node : group.getChildren()) {
            FxUtils.enableSubchildSelector(node, subchildIdx);
            node.applyCss();
        }
    }

    @Override
    public void removeFromGroup(Group splineGroup) {
        splineGroup.getChildren().remove(group);
    }

    public WpilibSpline(List<Waypoint> waypoints, Path path) {
        super(waypoints);
        this.path = path;
    }

    @Override
    public void update() {
        group.getChildren().clear();

        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint segStart = waypoints.get(i - 1);
            Waypoint segEnd = waypoints.get(i);

            QuinticHermiteSpline quintic = getQuinticSplinesFromWaypoints(new Waypoint[]{segStart, segEnd})[0];
            SplineSegment seg = new SplineSegment(segStart, segEnd, path);

            for (int sample = 0; sample <= 40; sample++) {
                PoseWithCurvature pose = quintic.getPoint(sample / 40.0);
                seg.getLine().getPoints().add(pose.poseMeters.getTranslation().getX());
                //Convert from WPILib to JavaFX coords
                seg.getLine().getPoints().add(-pose.poseMeters.getTranslation().getY());
            }

            seg.getLine().strokeWidthProperty().bind(strokeWidth);
            seg.getLine().getStyleClass().addAll("path");

            FxUtils.enableSubchildSelector(seg.getLine(), subchildIdx);
            seg.getLine().applyCss();

            group.getChildren().add(seg.getLine());
        }
    }

    @Override
    public void addToGroup(Group splineGroup, double scaleFactor) {
        strokeWidth.set(scaleFactor);
        splineGroup.getChildren().add(group);
        group.toBack();
    }

    @Override
    public boolean writeToFile(java.nio.file.Path path) {
        try {
            var values = ProjectPreferences.getInstance().getValues();

            TrajectoryConfig config = new TrajectoryConfig(values.getMaxVelocity(), values.getMaxAcceleration())
                    .setKinematics(new DifferentialDriveKinematics(values.getWheelBase()));
            Trajectory traj = trajectoryFromWaypoints(waypoints, config);

            TrajectoryUtil.toPathweaverJson(traj, path.resolve(".wpilib.json"));

            return true;
        } catch (IOException except) {
            LOGGER.log(Level.WARNING, "Could not write Spline to file", except);
            return false;
        }
    }

    private static QuinticHermiteSpline[] getQuinticSplinesFromWaypoints(Waypoint[] waypoints) {
        QuinticHermiteSpline[] splines = new QuinticHermiteSpline[waypoints.length - 1];
        for (int i = 0; i < waypoints.length - 1; i++) {    
            var p0 = waypoints[i];
            var p1 = waypoints[i + 1];

            double[] xInitialVector =
                    {p0.getX(), p0.getTangentX(), 0.0};
            double[] xFinalVector =
                    {p1.getX(), p1.getTangentX(), 0.0};
            double[] yInitialVector =
                    {p0.getY(), p0.getTangentY(), 0.0};
            double[] yFinalVector =
                    {p1.getY(), p1.getTangentY(), 0.0};

            splines[i] = new QuinticHermiteSpline(xInitialVector, xFinalVector,
                    yInitialVector, yFinalVector);
        }

        return splines;
    }

    private static Trajectory trajectoryFromWaypoints(Iterable<Waypoint> waypoints, TrajectoryConfig config) {
        var list = new TrajectoryGenerator.ControlVectorList();

        for(Waypoint wp: waypoints) {
            list.add(new Spline.ControlVector(
                    new double[] {wp.getX(), wp.getTangentX(), 0},
                    new double[] {wp.getY(), wp.getTangentY(), 0}));
        }

        return TrajectoryGenerator.generateTrajectory(list, config);
    }
}
