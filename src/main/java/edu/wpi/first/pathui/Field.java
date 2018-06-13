package edu.wpi.first.pathui;



import si.uom.SI;
import tec.units.indriya.quantity.Quantities;

import javafx.scene.image.Image;

import javax.measure.Quantity;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.Unit;

import static systems.uom.common.USCustomary.FOOT;
import static systems.uom.common.USCustomary.METER;


public class Field {
    private Image image= new Image("edu/wpi/first/pathui/2018-field.jpg");
    Quantity<Length> widthReal = Quantities.getQuantity(54,FOOT);
    Quantity<Length> heightReal = Quantities.getQuantity(27,FOOT);
    private int x=0;
    private int y=0;


    public Field(){


    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Number getRealWidth() {
        return widthReal.getValue();
    }

    public void setRealWidth(double width,String unit) {
        this.widthReal = Quantities.getQuantity(width, unit);
    }

    public double getRealHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
