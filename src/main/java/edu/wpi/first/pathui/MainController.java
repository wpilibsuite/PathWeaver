package edu.wpi.first.pathui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class MainController {
  @FXML private ImageView backgroundImage;
  @FXML private StackPane stack;
  @FXML private Pane drawPane;


  private Waypoint start;
  private Waypoint end;

  @FXML
  private void initialize() {
    stack.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    //backgroundImage.setImage(new Image("edu/wpi/first/pathui/000241.jpg"));
    drawPane.setOnDragOver(event -> {
      Dragboard dragboard = event.getDragboard();
      Waypoint wp = Waypoint.currentWaypoint;
      if (dragboard.getString().equals("point")) {
        wp.setX(event.getX());
        wp.setY(event.getY());
      } else if (dragboard.getString().equals("vector")) {
        Point2D pt = new Point2D(event.getX(), event.getY());
        wp.setTangent(pt.subtract(wp.getX(), wp.getY()));
        wp.lockTangent();
        if (wp.getPreviousSpline() != null) {
          wp.getPreviousSpline().updateControlPoints();
        }
        if (wp.getNextSpline() != null) {
          wp.getNextSpline().updateControlPoints();
        }
      }
      event.consume();
    });


    start = new Waypoint(100, 100, false);
    start.setTheta(0);
    end = new Waypoint(500, 500, false);
    end.setTheta(3.14 / 2);
    drawPane.getChildren().add(start.getTangentLine());
    drawPane.getChildren().add(end.getTangentLine());
    drawPane.getChildren().add(start.getDot());
    drawPane.getChildren().add(end.getDot());
    start.setNextWaypoint(end);
    end.setPreviousWaypoint(start);


    Waypoint middle = addNewWaypoint(start, end);
    Waypoint second = addNewWaypoint(start, middle);
    Waypoint fourth = addNewWaypoint(middle, end);
    start.setTangent(new Point2D(200, 0));
    end.setTangent(new Point2D(0, 200));
    createCurve(start, second);
    createCurve(second, middle);
    createCurve(middle, fourth);
    createCurve(fourth, end);
    for (Waypoint waypoint = start; waypoint != null; waypoint = waypoint.getNextWaypoint()) {
      waypoint.update();
    }
  }

  void createCurve(Waypoint start, Waypoint end) {
    Spline curve = new Spline(start, end);
    drawPane.getChildren().add(curve.getCubic());
    curve.getCubic().toBack();
  }


  Waypoint addNewWaypoint(Waypoint previous, Waypoint next) {
    if (previous.getNextWaypoint() != next || next.getPreviousWaypoint() != previous) {
      throw new RuntimeException("New Waypoint not between connected points");
    }
    Waypoint newPoint = new Waypoint((previous.getX() + next.getX()) / 2, (previous.getY() + next.getY()) / 2, false);
    newPoint.setPreviousWaypoint(previous);
    newPoint.setNextWaypoint(next);
    next.setPreviousWaypoint(newPoint);
    previous.setNextWaypoint(newPoint);
    drawPane.getChildren().add(newPoint.getTangentLine());
    drawPane.getChildren().add(newPoint.getDot());
    return newPoint;
  }


}
