package edu.wpi.first.pathui;

import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class Waypoint {
  private Waypoint previousWaypoint = null;
  private Waypoint nextWaypoint = null;
  private DoubleProperty x = new SimpleDoubleProperty();
  private DoubleProperty y = new SimpleDoubleProperty();
  private boolean lockTheta;
  private Spline previousSpline = null;
  private Spline nextSpline = null;
  private ObjectProperty<Point2D> tangent = new SimpleObjectProperty<>();

  private DoubleProperty theta = new SimpleDoubleProperty();



  public static Waypoint currentWaypoint = null;


  private Line tangentLine;

  private Circle dot;

  public Waypoint(double xPosition, double yPosition,boolean fixedAngle){
    lockTheta = fixedAngle;
    setX(xPosition);
    setY(yPosition);
    dot = new Circle(10);
    dot.centerXProperty().bind(x);
    dot.centerYProperty().bind(y);
    dot.setOnDragDetected(event -> {
      currentWaypoint = this;
      Dragboard board = dot.startDragAndDrop(TransferMode.MOVE);
      board.setContent(Map.of(DataFormat.PLAIN_TEXT,"point"));
    });
    x.addListener(__->update());
    y.addListener(__->update());

    tangentLine = new Line();
    tangentLine.startXProperty().bind(x);
    tangentLine.startYProperty().bind(y);
    tangent.set(new Point2D(500,0));
    tangentLine.endXProperty().bind(Bindings.createObjectBinding(()->this.getTangent().getX() + this.getX(),tangent,x));
    tangentLine.endYProperty().bind(Bindings.createObjectBinding(()->this.getTangent().getY() + this.getY(),tangent,y));
  }
  private void update(){
    updateTheta();
    if(previousWaypoint != null){
      previousWaypoint.updateTheta();
      getPreviousSpline().updateControlPoints();
      if(previousWaypoint.getPreviousSpline() != null){
        previousWaypoint.getPreviousSpline().updateControlPoints();
      }
    }
    if(nextWaypoint != null){
      nextWaypoint.updateTheta();
      getNextSpline().updateControlPoints();
      if(nextWaypoint.getNextSpline() != null){
        nextWaypoint.getNextSpline().updateControlPoints();
      }
    }



  }
  public void updateTheta(){
    if(lockTheta){
      //System.out.println("locked");
      return;
    }
    if(previousWaypoint == null){
      //System.out.println("previous");
      return;
    }
    if(nextWaypoint == null){
      //System.out.println("next");
      return;
    }

    Point2D p1 = new Point2D(previousWaypoint.getX(),previousWaypoint.getY());
    Point2D p2 = new Point2D(this.getX(),this.getY());
    Point2D p3 = new Point2D(nextWaypoint.getX(),nextWaypoint.getY());

    Point2D p1_shift = new Point2D(0,0);
    Point2D p2_shift = p2.subtract(p1).multiply(1/p3.distance(p1));
    Point2D p3_shift = p3.subtract(p1).multiply(1/p3.distance(p1));

    //refactor later
    Point2D q = new Point2D(0,0);
    Point2D r = new Point2D(p2_shift.getX()*p3_shift.getX() + p2_shift.getY()*p3_shift.getY(),

        -p2_shift.getX()*p3_shift.getY() + p2_shift.getY()*p3_shift.getX());
    Point2D s = new Point2D(1,0);

    double beta = 1 - 2 * r.getX();
    double gamma = Math.pow((4 * (r.getX()-Math.pow(r.distance(p1_shift),2)) - 3),3)/27;
    double lambda = Math.pow(-gamma,1/6);

    double phi1 = Math.atan2(Math.sqrt(-gamma - Math.pow(beta,2)),beta)/3;
    double ur = lambda * Math.cos(phi1);
    double ui = lambda * Math.sin(phi1);
    double phi2 = Math.atan2(-Math.sqrt(-gamma - Math.pow(beta,2)),beta)/3;

    double zr = lambda * Math.cos(phi2);
    double zi = lambda * Math.sin(phi2);

    double t1 = 1.0/2 + ur + zr/2;
    double t2 = 1.0/2 - (1.0/4) * (ur + zr + Math.sqrt(3)*(ui-zi));
    double t3 = 1.0/2 - (1.0/4) * (ur + zr - Math.sqrt(3)*(ui-zi));

    double t;
    if(t1 > 0 && t1 < 1){
      t = t1;
    }else if(t2 > 0 && t2 < 1){
      t = t2;
    }else {
      t = t3;
    }

    Point2D a1 = (p2.subtract(p1).subtract(p3.subtract(p1).multiply(t))).multiply(1/(t*t -t));
    Point2D a2 = p3.subtract(p1).subtract(a1);

    tangent.set(a1.multiply(2*t).add(a2));
    System.out.println(getTangent());


    double newTheta = Math.atan2(getTangent().getY(),getTangent().getX());
    setTheta(newTheta);
  }
  public void addSpline(Spline newSpline,boolean amFirst){
    if(amFirst){
      nextSpline = newSpline;
      nextSpline.getCubic().startXProperty().bind(x);
      nextSpline.getCubic().startYProperty().bind(y);
    }
    if(!amFirst){
      previousSpline = newSpline;
      previousSpline.getCubic().endXProperty().bind(x);
      previousSpline.getCubic().endYProperty().bind(y);
    }
  }

  public Line getTangentLine() {
    return tangentLine;
  }

  public Point2D getTangent() {
    return tangent.get();
  }
  public ObjectProperty<Point2D> tangentProperty() {
    return tangent;
  }

  public void setTangent(Point2D tangent) {
    this.tangent.set(tangent);
  }

  public Spline getPreviousSpline() {
    return previousSpline;
  }

  public Spline getNextSpline() {
    return nextSpline;
  }

  public double getTheta() {
    return theta.get();
  }

  public DoubleProperty thetaProperty() {
    return theta;
  }

  public void setTheta(double theta) {
    this.theta.set(theta);
  }

  public Circle getDot() {
    return dot;
  }

  public double getX() {
    return x.get();
  }

  public DoubleProperty xProperty() {
    return x;
  }

  public void setX(double x) {
    this.x.set(x);
  }

  public double getY() {
    return y.get();
  }

  public DoubleProperty yProperty() {
    return y;
  }

  public void setY(double y) {
    this.y.set(y);
  }

  public Waypoint getPreviousWaypoint() {
    return previousWaypoint;
  }

  public void setPreviousWaypoint(Waypoint previousWaypoint) {
    System.out.println("previous is " + previousWaypoint.toString());
    this.previousWaypoint = previousWaypoint;
  }

  public Waypoint getNextWaypoint() {
    return nextWaypoint;
  }

  public void setNextWaypoint(Waypoint nextWaypoint) {
    this.nextWaypoint = nextWaypoint;
  }

}
