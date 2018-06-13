package edu.wpi.first.pathui;



import si.uom.SI;
import tec.units.indriya.quantity.Quantities;

import javafx.scene.image.Image;

import javax.measure.Quantity;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.Unit;


import static tec.units.indriya.unit.Units.METRE;

public class Field {
    private Image image= new Image("edu/wpi/first/pathui/2018-field.jpg");
    private double width=54;
    private double height=27;
    Quantity<Length> heightReal = Quantities.getQuantity(5,METRE);



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

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
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
