package edu.wpi.first.pathui;


import javafx.scene.image.Image;
import tec.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import java.awt.geom.Point2D;

import static edu.wpi.first.pathui.PathUnits.FOOT;


public class Field {
    private Image image = new Image("edu/wpi/first/pathui/2018-field.jpg");
    private Quantity<Length> rWidth = Quantities.getQuantity(54,FOOT);
    private Quantity<Length> rLength = Quantities.getQuantity(27,FOOT);
    private double scale = 16.5;
    private Point2D coord = new Point2D.Double(450,175);
    public Unit<Length> unit = FOOT;
    public Unit<Length> Pixel;
    public Field() {
        Pixel = PathUnits.addUnit(unit.multiply(scale),"Pixel","pix");
    }

    public Field(Image image,Unit<Length> u, double realWidth, double realLength, double xPixel, double yPixel, double pixelWidth, double pixelLength) {
        setImage(image);
        setRealWidth(Quantities.getQuantity(54,FOOT));
        setRealLength(Quantities.getQuantity(27,FOOT));
        setCoord(new Point2D.Double(xPixel,yPixel));
        setScale(((pixelWidth/realWidth)+(pixelLength/realLength))/2);
        setUnit(u);
        Unit<Length> Pixel = PathUnits.addUnit(unit.multiply(scale),"Pixel","pix");
    }


    public Image getImage() {
        return image;
    }

    private void setImage(Image image) {
        this.image = image;
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
