package edu.wpi.first.pathweaver.spline;

import java.util.Arrays;
import java.util.List;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.spline.PoseWithCurvature;
import edu.wpi.first.pathweaver.Waypoint;
import edu.wpi.first.pathweaver.global.CurrentSelections;
import edu.wpi.first.pathweaver.path.Path;
import javafx.scene.shape.Polyline;

public class SplineSegment {
    private final Polyline centerLine = new Polyline();
    private final Polyline leftLine = new Polyline();
    private final Polyline rightLine = new Polyline();
    
    private Waypoint start;
    private Waypoint end;

    public SplineSegment(Waypoint start, Waypoint end, Path path) {
        this.start = start;
        this.end = end;
        centerLine.setOnDragDetected(event -> {
            CurrentSelections.setCurSplineStart(this.start);
            CurrentSelections.setCurSplineEnd(this.end);
            CurrentSelections.setCurPath(path);
        });

        centerLine.setOnMouseClicked(event -> {
            CurrentSelections.setCurPath(path);
            event.consume();
        });
    }

    /**
     * Add points to the center line given the pose.  Also add points
     * to the left and right lines using the pose and the path width
     * @param pose the pose at the current path point
     * @param pathWidth the path width
     */
    public void addPoints(PoseWithCurvature pose, double pathWidth) {
        //Convert from WPILib to JavaFX coords
        final double yCoordAdjustment = -1;
        double x = pose.poseMeters.getTranslation().getX();
        double y = pose.poseMeters.getTranslation().getY();
        getCenterLine().getPoints().add(x);
        getCenterLine().getPoints().add(y * yCoordAdjustment);
        
        Rotation2d rotation2d = pose.poseMeters.getRotation();
        double x1 = x - (pathWidth / 2 * rotation2d.getSin());
        double y1 = y + (pathWidth / 2 * rotation2d.getCos());
        double x2 = x + (pathWidth / 2 * rotation2d.getSin());
        double y2 = y - (pathWidth / 2 * rotation2d.getCos());
        getLeftLine().getPoints().add(x1);
        getLeftLine().getPoints().add(y1 * yCoordAdjustment);
        getRightLine().getPoints().add(x2);
        getRightLine().getPoints().add(y2 * yCoordAdjustment);
    }

    public Polyline getCenterLine() {
        return centerLine;
    }

    public Polyline getLeftLine() {
        return leftLine;
    }

    public Polyline getRightLine() {
        return rightLine;
    }

    public List<Polyline> getLines() {
        return Arrays.asList( centerLine, leftLine, rightLine );
    }

    public Waypoint getStart() {
        return start;
    }

    public Waypoint getEnd() {
        return end;
    }

    public void setStart(Waypoint start) {
        this.start = start;
    }

    public void setEnd(Waypoint end) {
        this.end = end;
    }
}
