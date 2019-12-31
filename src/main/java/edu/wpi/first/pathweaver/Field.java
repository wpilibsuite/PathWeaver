package edu.wpi.first.pathweaver;

import java.util.function.Supplier;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

public class Field {
	private Supplier<Image> imageSupplier;
	private Image image;
	private Quantity<Length> rWidth;
	private Quantity<Length> rLength;
	private double xPixel;
	private double yPixel;
	private double pixelWidth;
	private double pixelLength;
	private double scale;
	private Point2D coord;
	public Unit<Length> unit;

	/**
	 * Creates a new Field Object.
	 *
	 * @param imageSupplier
	 *            a supplier for image of the field
	 * @param unit
	 *            unit which the field is measured
	 * @param realWidth
	 *            width of field in real units
	 * @param realLength
	 *            length of field in real units
	 * @param xPixel
	 *            x pixel top left x pixels
	 * @param yPixel
	 *            y pixel top left y pixels
	 * @param pixelWidth
	 *            width of drawable area in pixels
	 * @param pixelLength
	 *            length of drawable area in pixels
	 */
	public Field(Supplier<Image> imageSupplier, Unit<Length> unit, double realWidth, double realLength, double xPixel,
			double yPixel, double pixelWidth, double pixelLength) {
		this.imageSupplier = imageSupplier;
		this.xPixel = xPixel;
		this.yPixel = yPixel;
		this.pixelWidth = pixelWidth;
		this.pixelLength = pixelLength;
		setRealWidth(Quantities.getQuantity(realWidth, unit));
		setRealLength(Quantities.getQuantity(realLength, unit));
		updateCoord();
		updateScale();
		setUnit(unit);
	}

	/**
	 * Creates a new Field Object.
	 *
	 * @param image
	 *            image of the field
	 * @param unit
	 *            unit which the field is measured
	 * @param realWidth
	 *            width of field in real units
	 * @param realLength
	 *            length of field in real units
	 * @param xPixel
	 *            x pixel top left x pixels
	 * @param yPixel
	 *            y pixel top left y pixels
	 * @param pixelWidth
	 *            width of drawable area in pixels
	 * @param pixelLength
	 *            length of drawable area in pixels
	 */
	public Field(Image image, Unit<Length> unit, double realWidth, double realLength, double xPixel, double yPixel,
			double pixelWidth, double pixelLength) {
		this(() -> image, unit, realWidth, realLength, xPixel, yPixel, pixelWidth, pixelLength);
	}

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

	public Point2D getCoord() {
		return coord;
	}

	private double realWidth() {
		return rWidth.getValue().doubleValue();
	}

	private double realLength() {
		return rLength.getValue().doubleValue();
	}

	private void updateCoord() {
		this.coord = new Point2D(xPixel + pixelWidth / 2 - realWidth() / 2,
				yPixel + pixelLength / 2 - realLength() / 2);
	}

	private void updateScale() {
		this.scale = ((pixelWidth / realWidth()) + (pixelLength / realLength())) / 2;
	}

	public Unit<Length> getUnit() {
		return unit;
	}

	private void setUnit(Unit<Length> unit) {
		this.unit = unit;
	}

	/**
	 * Converts the Field from the current Unit systems to the supplied Unit system.
	 *
	 * @param unit
	 *            The unit system to convert the Field to.
	 */
	public void convertUnit(Unit<Length> unit) {
		setUnit(unit);
		setRealWidth(rWidth.to(unit));
		setRealLength(rLength.to(unit));
		updateCoord();
		updateScale();
	}
}
