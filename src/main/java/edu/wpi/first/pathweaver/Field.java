package edu.wpi.first.pathweaver;

import tec.units.indriya.quantity.Quantities;

import java.util.function.Supplier;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;


public class Field {
  private Supplier<Image> imageSupplier;
  private Image image;
  private Quantity<Length> rWidth;
  private Quantity<Length> rLength;
  private double scale;
  private Point2D coord;
  public Unit<Length> unit;
  public Unit<Length> pixel;

  /**
   * Creates a new Field Object.
   *
   * @param imageSupplier a supplier for image of the field
   * @param unit          unit which the field is measured
   * @param realWidth     width of field in real units
   * @param realLength    length of field in real units
   * @param xPixel        x pixel top left x pixels
   * @param yPixel        y pixel top left y pixels
   * @param pixelWidth    width of drawable area in pixels
   * @param pixelLength   length of drawable area in pixels
   */
  public Field(Supplier<Image> imageSupplier,
               Unit<Length> unit,
               double realWidth, double realLength,
               double xPixel, double yPixel,
               double pixelWidth, double pixelLength) {
    this.imageSupplier = imageSupplier;
    setRealWidth(Quantities.getQuantity(realWidth, unit));
    setRealLength(Quantities.getQuantity(realLength, unit));
    setCoord(new Point2D(xPixel + pixelWidth / 2 - realWidth / 2, yPixel + pixelLength / 2 - realLength / 2));
    setScale(((pixelWidth / realWidth) + (pixelLength / realLength)) / 2); //NOPMD Useless Parentheses
    setUnit(unit);
    pixel = PathUnits.addUnit(unit.multiply(scale), "Pixel", "px");
  }

  /**
   * Creates a new Field Object.
   *
   * @param image       image of the field
   * @param unit        unit which the field is measured
   * @param realWidth   width of field in real units
   * @param realLength  length of field in real units
   * @param xPixel      x pixel top left x pixels
   * @param yPixel      y pixel top left y pixels
   * @param pixelWidth  width of drawable area in pixels
   * @param pixelLength length of drawable area in pixels
   */
  public Field(Image image,
               Unit<Length> unit,
               double realWidth, double realLength,
               double xPixel, double yPixel,
               double pixelWidth, double pixelLength) {
    this(() -> image, unit, realWidth, realLength, xPixel, yPixel, pixelWidth, pixelLength);
  }


  /**
   * Gets the field image.
   */
  public Image getImage() {
    if (image == null) {
      image = imageSupplier.get();
    }
    return image;
  }

  public Quantity<Length> getRealWidth() {
    return rWidth;
  }

  private void setRealWidth(Quantity<Length> width) {
    this.rWidth = width;
  }

  public Quantity<Length> getRealLength() {
    return rLength;
  }

  private void setRealLength(Quantity<Length> length) {
    this.rLength = length;
  }

  public double getScale() {
    return scale;
  }

  private void setScale(double scale) {
    this.scale = scale;
  }

  public Point2D getCoord() {
    return coord;
  }

  private void setCoord(Point2D coord) {
    this.coord = coord;
  }

  public Unit<Length> getUnit() {
    return unit;
  }

  private void setUnit(Unit<Length> unit) {
    this.unit = unit;
  }
}
