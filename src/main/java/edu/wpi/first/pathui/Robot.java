package edu.wpi.first.pathui;

import javax.measure.Unit;
import javax.measure.quantity.Length;

public class Robot {
  private double wheelBaseWidth;
  private double chassisWidth;
  private double chassisLength;
  private double maxVelocity;
  private double maxAcceleration;
  private double maxJerk;
  private double timeStep;
  public Unit<Length> unit;





  public double getWheelBaseWidth() {
    return wheelBaseWidth;
  }

  public void setWheelBaseWidth(double wheelBaseWidth) {
    this.wheelBaseWidth = wheelBaseWidth;
  }

  public double getChassisWidth() {
    return chassisWidth;
  }

  public void setChassisWidth(double chassisWidth) {
    this.chassisWidth = chassisWidth;
  }

  public double getChassisLength() {
    return chassisLength;
  }

  public void setChassisLength(double chassisLength) {
    this.chassisLength = chassisLength;
  }

  public double getMaxVelocity() {
    return maxVelocity;
  }

  public void setMaxVelocity(double maxVelocity) {
    this.maxVelocity = maxVelocity;
  }

  public double getMaxAcceleration() {
    return maxAcceleration;
  }

  public void setMaxAcceleration(double maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  public double getMaxJerk() {
    return maxJerk;
  }

  public void setMaxJerk(double maxJerk) {
    this.maxJerk = maxJerk;
  }

  public double getTimeStep() {
    return timeStep;
  }

  public void setTimeStep(double timeStep) {
    this.timeStep = timeStep;
  }

  public Unit<Length> getUnit() {
    return unit;
  }

  public void setUnit(Unit<Length> unit) {
    this.unit = unit;
  }
}
