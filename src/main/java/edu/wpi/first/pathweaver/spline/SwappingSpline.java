package edu.wpi.first.pathweaver.spline;

import edu.wpi.first.pathweaver.DataFormats;
import edu.wpi.first.pathweaver.DragHandler;
import edu.wpi.first.pathweaver.PathDisplayController;
import edu.wpi.first.pathweaver.Waypoint;
import javafx.scene.Group;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.Map;

public class SwappingSpline implements Spline {

  private final PathfinderSpline pfSpline;
  private final QuickSpline qSpline;

  private Spline activeSpline;

  private final Group group = new Group();

  private double scaleFactor;
  private final Waypoint start;

  /**
   * A SwappingSpline switches between a PathfinderSpline and a QuickSpline when appropriate methods are called.
   * This exists because the generation of Pathfinder paths is intensive and doesn't allow for easy dragging.
   * @param start The Waypoint at the start of the Spline.
   * @param end The Waypoint at the end of the Spline.
   */
  public SwappingSpline(Waypoint start, Waypoint end) {
    this.start = start;
    pfSpline = new PathfinderSpline(start, end, this);
    qSpline = new QuickSpline(start, end, this);
    activeSpline = pfSpline;
    group.setOnDragDetected(event -> {
      Dragboard board = group.startDragAndDrop(TransferMode.ANY);
      board.setContent(Map.of(DataFormats.SPLINE, "Spline"));
      DragHandler.setCurrentSpline(this);
    });
  }

  @Override
  public void update() {
    activeSpline.update();
  }

  @Override
  public void addToGroup(Group splineGroup, double scaleFactor) {
    this.scaleFactor = scaleFactor;
    splineGroup.getChildren().add(group);
    activeSpline.addToGroup(group, scaleFactor);
  }

  @Override
  public void setEnd(Waypoint end) {
    pfSpline.setEnd(end);
    qSpline.setEnd(end);
  }

  @Override
  public void enableSubchildSelector(int i) {
    pfSpline.enableSubchildSelector(i);
    qSpline.enableSubchildSelector(i);
  }

  @Override
  public void removeFromGroup(Group splineGroup) {
    splineGroup.getChildren().remove(group);
  }

  @Override
  public void addPathWaypoint(PathDisplayController controller) {
    activeSpline.addPathWaypoint(controller);
    start.getPath().swapToQuick();
  }

  /**
   * Swaps the displayed path to PathfinderSpline.
   */
  public void swapToPathfinder() {
    activeSpline = pfSpline;
    qSpline.removeFromGroup(group);
    group.getChildren().clear();
    pfSpline.addToGroup(group, scaleFactor);
    pfSpline.update();
  }

  /**
   * Swaps the displayed path to QuickSpline.
   */
  public void swapToQuick() {
    activeSpline = qSpline;
    pfSpline.removeFromGroup(group);
    group.getChildren().clear();
    qSpline.addToGroup(group, scaleFactor);
    qSpline.update();
  }

}
