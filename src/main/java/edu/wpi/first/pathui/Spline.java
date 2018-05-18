package edu.wpi.first.pathui;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Pair;

public class Spline {
  private CubicCurve cubic;
  private Waypoint start;
  private Waypoint end;
  public Spline(Waypoint first,Waypoint last){
    start = first;
    end = last;
    cubic = new CubicCurve();
    start.addSpline(this,true);
    end.addSpline(this,false);

    cubic.setStroke(Color.FORESTGREEN);
    cubic.setStrokeWidth(4);
    cubic.setStrokeLineCap(StrokeLineCap.ROUND);
    cubic.setFill(Color.TRANSPARENT);

    updateControlPoints();
  }
  public void updateControlPoints(){
    Pair<Point2D, Point2D> points = computeControlPoints();
    cubic.setControlX1(points.getKey().getX());
    cubic.setControlY1(points.getKey().getY());
    cubic.setControlX2(points.getValue().getX());
    cubic.setControlY2(points.getValue().getY());
  }
  private Pair<Point2D, Point2D> computeControlPoints(){
    double xdiff = start.getX() - end.getX();
    double ydiff = start.getY() - end.getY();
    double distance = Math.sqrt(xdiff*xdiff+ydiff*ydiff);
    Point2D control1 = new Point2D(start.getX() + (start.getTangent().getX())/ 3,
        start.getY() + (start.getTangent().getY())/3);
    Point2D control2 = new Point2D(end.getX() - 2 * (end.getTangent().getX())/3,
        end.getY() - 2 * (end.getTangent().getY())/3);
    Pair<Point2D, Point2D> controlPoints = new Pair<Point2D, Point2D>(control1,control2);
    return controlPoints;
  }

  public CubicCurve getCubic() {
    return cubic;
  }

  public Waypoint getStart() {
    return start;
  }

  public void setStart(Waypoint start) {
    this.start = start;
  }

  public Waypoint getEnd() {
    return end;
  }

  public void setEnd(Waypoint end) {
    this.end = end;
  }
}
