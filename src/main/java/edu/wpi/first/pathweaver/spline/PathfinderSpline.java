package edu.wpi.first.pathweaver;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.modifiers.TankModifier;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class PathfinderSpline implements Spline {

  private final Waypoint start;
  private Waypoint end;
  private final Group group;
  private double strokeWidth = 1.0;
  private int subchildIdx = 0;

  private final Queue<Line> lines = new ArrayDeque<>(); // Object pool of lines


  /**
   * Constructs a spline that is represented by a Pathfinder V1 path.
   * @param start The waypoint at the start of spline.
   * @param end The waypoint at the end of spline.
   */
  public PathfinderSpline(Waypoint start, Waypoint end) {
    this.start = start;
    this.end = end;
    group = new Group();
    group.setOnDragDetected(event -> {
      Dragboard board = group.startDragAndDrop(TransferMode.ANY);
      board.setContent(Map.of(DataFormats.SPLINE, "Spline"));
      DragHandler.setCurrentSpline(this);
    });
  }

  @Override
  public void update() {
    removeAndFreeLines();
    TankModifier tank = start.getPath().getTankModifier();
    ArrayList<Trajectory.Segment> center = new ArrayList<>();
    ArrayList<Trajectory.Segment> left = new ArrayList<>();
    ArrayList<Trajectory.Segment> right = new ArrayList<>();
    // filter points relevant to this spline
    boolean foundStart = false;
    for (int i = 0; i < tank.getSourceTrajectory().length(); i++) {
      Trajectory.Segment source = tank.getSourceTrajectory().get(i);
      if (fuzzyEquals(source, start)) {
        foundStart = true;
      }
      if (foundStart) {
        center.add(source);
        left.add(tank.getLeftTrajectory().get(i));
        right.add(tank.getRightTrajectory().get(i));
      }
      if (fuzzyEquals(source, end) && end.getPath().getEnd() != end) {
        // Have filtered all our points
        break;
      }
    }
    createLines(center, left, right);
    updateColors();
  }

  private boolean fuzzyEquals(Trajectory.Segment seg, Waypoint wp) {
    double error = 1.0;
    return Math.abs(seg.x - wp.getX()) < error && Math.abs(seg.y - wp.getY()) < error;
  }

  private void createLines(List<Trajectory.Segment> center, List<Trajectory.Segment> left,
                           List<Trajectory.Segment> right) {
    for (int i = 1; i < center.size(); i++) {
      Line centerLine = createLine(center, i, "path");
      Line leftLine = createLine(left, i, "path", "tank");
      Line rightLine = createLine(right, i, "path", "tank");
      group.getChildren().addAll(centerLine, leftLine, rightLine);
    }
  }

  private Line createLine(List<Trajectory.Segment> segments, int index, String... cssClass) {
    Trajectory.Segment current = segments.get(index - 1);
    Trajectory.Segment next = segments.get(index);
    Line line = getFreeLine();
    line.setStartX(current.x);
    line.setStartY(current.y);
    line.setEndX(next.x);
    line.setEndY(next.y);
    line.getStyleClass().addAll(cssClass);
    line.setStrokeWidth(strokeWidth);
    return line;
  }

  private void updateColors() {
    for (Node node : group.getChildren()) {
      FxUtils.enableSubchildSelector(node, subchildIdx);
      node.applyCss();
    }
  }



  @Override
  public void addToGroup(Group splineGroup, double scaleFactor) {
    strokeWidth = scaleFactor;
    splineGroup.getChildren().add(group);
    group.toBack();
    for (Node node : group.getChildren()) {
      Line line = (Line) node;
      line.setStrokeWidth(scaleFactor);
    }
  }

  @Override
  public void setEnd(Waypoint end) {
    this.end = end;
  }

  @Override
  public void enableSubchildSelector(int i) {
    this.subchildIdx = i;
    updateColors();
  }

  @Override
  public void removeFromGroup(Group splineGroup) {
    splineGroup.getChildren().remove(group);
  }

  @Override
  public void addPathWaypoint(PathDisplayController controller) {
    Waypoint newPoint = start.getPath().addNewWaypoint(this);
    controller.addWaypointToPane(newPoint);
    controller.setupWaypoint(newPoint);
    controller.selectWaypoint(newPoint, false);
    Waypoint.currentWaypoint = newPoint;
  }

  private Line getFreeLine() {
    if (lines.isEmpty()) {
      return new Line();
    } else {
      return lines.remove();
    }
  }

  private void removeAndFreeLines() {
    for (Node n : group.getChildren()) {
      n.getStyleClass().clear();
      lines.add((Line) n);
    }
    group.getChildren().clear();
  }
}
