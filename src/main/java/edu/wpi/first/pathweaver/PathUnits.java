package edu.wpi.first.pathweaver;

import si.uom.SI;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.measure.quantity.*;

public final class PathUnits extends AbstractSystemOfUnits {
	private static final String SYSTEM_NAME = "PathWeaver Units";
	private static final PathUnits INSTANCE = new PathUnits();

	public static final Unit<Time> SECOND = addUnit(SI.SECOND, "Second", "sec");

	public static final Unit<Length> INCH = addUnit(USCustomary.INCH, "Inch", "in");
	public static final Unit<Length> FOOT = addUnit(USCustomary.FOOT, "Foot", "ft");
	public static final Unit<Length> YARD = addUnit(USCustomary.YARD, "Yard", "yd");
	public static final Unit<Length> METER = addUnit(SI.METRE, "Meter", "m");
	public static final Unit<Length> CENTIMETER = addUnit(MetricPrefix.CENTI(USCustomary.METER), "Centimeter", "cm");

	public static final List<Unit<Length>> LENGTHS = Collections
			.unmodifiableList(List.of(INCH, FOOT, YARD, METER, CENTIMETER));
	// public static final List<Unit<Length>> LENGTHS =
	// Collections.unmodifiableList(List.of(METER));

	public static final Unit<Speed> INCH_PER_SECOND = addUnit(INCH.divide(SECOND)).asType(Speed.class);
	public static final Unit<Speed> FOOT_PER_SECOND = addUnit(FOOT.divide(SECOND)).asType(Speed.class);
	public static final Unit<Speed> YARD_PER_SECOND = addUnit(YARD.divide(SECOND)).asType(Speed.class);
	public static final Unit<Speed> METER_PER_SECOND = addUnit(METER.divide(SECOND)).asType(Speed.class);
	public static final Unit<Speed> CENTIMETER_PER_SECOND = addUnit(CENTIMETER.divide(SECOND)).asType(Speed.class);

	public static final List<Unit<Speed>> SPEEDS = Collections.unmodifiableList(
			List.of(INCH_PER_SECOND, FOOT_PER_SECOND, YARD_PER_SECOND, METER_PER_SECOND, CENTIMETER_PER_SECOND));

	public static final Unit<Acceleration> INCH_PER_SQUARE_SECOND = addUnit(INCH_PER_SECOND.divide(SECOND))
			.asType(Acceleration.class);
	public static final Unit<Acceleration> FOOT_PER_SQUARE_SECOND = addUnit(FOOT_PER_SECOND.divide(SECOND))
			.asType(Acceleration.class);
	public static final Unit<Acceleration> YARD_PER_SQUARE_SECOND = addUnit(YARD_PER_SECOND.divide(SECOND))
			.asType(Acceleration.class);
	public static final Unit<Acceleration> METER_PER_SQUARE_SECOND = addUnit(METER_PER_SECOND.divide(SECOND))
			.asType(Acceleration.class);
	public static final Unit<Acceleration> CENTIMETER_PER_SQUARE_SECOND = addUnit(CENTIMETER_PER_SECOND.divide(SECOND))
			.asType(Acceleration.class);

	public static final List<Unit<Acceleration>> ACCELERATIONS = Collections
			.unmodifiableList(List.of(INCH_PER_SQUARE_SECOND, FOOT_PER_SQUARE_SECOND, YARD_PER_SQUARE_SECOND,
					METER_PER_SQUARE_SECOND, CENTIMETER_PER_SQUARE_SECOND));

	public static PathUnits getInstance() {
		return INSTANCE;
	}

	public Unit<Speed> speedUnit(Unit<Length> length) {
		if (length == INCH) {
			return INCH_PER_SECOND;
		} else if (length == FOOT) {
			return FOOT_PER_SECOND;
		} else if (length == YARD) {
			return YARD_PER_SECOND;
		} else if (length == METER) {
			return METER_PER_SECOND;
		} else if (length == CENTIMETER) {
			return CENTIMETER_PER_SECOND;
		}

		throw new IllegalArgumentException();
	}

	public Unit<Acceleration> accelerationUnit(Unit<Length> length) {
		if (length == INCH) {
			return INCH_PER_SQUARE_SECOND;
		} else if (length == FOOT) {
			return FOOT_PER_SQUARE_SECOND;
		} else if (length == YARD) {
			return YARD_PER_SQUARE_SECOND;
		} else if (length == METER) {
			return METER_PER_SQUARE_SECOND;
		} else if (length == CENTIMETER) {
			return CENTIMETER_PER_SQUARE_SECOND;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public String getName() {
		return SYSTEM_NAME;
	}

	/**
	 * Gets the unit of length with the given name, case-insensitive.
	 *
	 * @param unit
	 *            the name of the unit of length to get
	 * @return the unit represented by the string
	 * @throws IllegalArgumentException
	 *             if no such unit exists
	 */
	public Unit<Length> length(String unit) {
		switch (unit.toLowerCase(Locale.US)) {
			// Metric
			case "meter" :
			case "metre" :
			case "meters" :
			case "metres" :
			case "m" :
				return METER;
			case "centimeter" :
			case "centimetre" :
			case "centimeters" :
			case "centimetres" :
			case "cm" :
				return CENTIMETER;
			// Imperial
			case "inch" :
			case "inches" :
			case "in" :
				return INCH;
			case "foot" :
			case "feet" :
			case "ft" :
				return FOOT;
			case "yard" :
			case "yards" :
			case "yd" :
				return YARD;
			default :
				throw new IllegalArgumentException("Unsupported length unit: " + unit);
		}
	}

	/**
	 * Adds a new unit not mapped to any specified quantity type.
	 *
	 * @param unit
	 *            the unit being added.
	 * @return <code>unit</code>.
	 */
	private static <U extends Unit<?>> U addUnit(U unit) {
		INSTANCE.units.add(unit);
		return unit;
	}

	/**
	 * Adds a new unit not mapped to any specified quantity type and puts a text as
	 * symbol or label.
	 *
	 * @param unit
	 *            the unit being added.
	 * @param name
	 *            the string to use as name
	 * @param text
	 *            the string to use as label or symbol
	 * @param isLabel
	 *            if the string should be used as a label or not
	 * @return <code>unit</code>.
	 */
	private static <U extends Unit<?>> U addUnit(U unit, String name, String text, boolean isLabel) {
		if (isLabel) {
			SimpleUnitFormat.getInstance().label(unit, text);
		}
		if (name != null && unit instanceof AbstractUnit) {
			return Helper.addUnit(INSTANCE.units, unit, name);
		} else {
			INSTANCE.units.add(unit);
		}
		return unit;
	}

	/**
	 * Adds a new unit not mapped to any specified quantity type and puts a text as
	 * symbol or label.
	 *
	 * @param unit
	 *            the unit being added.
	 * @param name
	 *            the string to use as name
	 * @param label
	 *            the string to use as label
	 * @return <code>unit</code>.
	 */
	private static <U extends Unit<?>> U addUnit(U unit, String name, String label) {
		return addUnit(unit, name, label, true);
	}
}
