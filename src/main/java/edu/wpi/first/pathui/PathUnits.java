package edu.wpi.first.pathui;

import systems.uom.common.USCustomary;
import tec.units.ri.AbstractSystemOfUnits;
import tec.units.ri.AbstractUnit;
import tec.units.ri.format.SimpleUnitFormat;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Time;
import javax.measure.spi.SystemOfUnits;

public final class PathUnits extends AbstractSystemOfUnits {
  private static final String SYSTEM_NAME = "Path UI Units";

  private static final PathUnits INSTANCE = new PathUnits();

  public static final Unit<Length> METER = addUnit(USCustomary.METER, "Meter", "m");
  public static final Unit<Length> INCH = addUnit(USCustomary.INCH, "Inch", "in");
  public static final Unit<Length> FOOT = addUnit(USCustomary.FOOT, "Foot", "ft");
  public static final Unit<Length> YARD = addUnit(USCustomary.YARD, "Yard", "yd");
  public static final Unit<Length> MILE = addUnit(USCustomary.MILE, "Mile", "mi");
  public static final Unit<Length> CENTIMETER = addUnit(METER.divide(100), "Centimeter", "cm");
  public static final Unit<Length> MILLIMETER = addUnit(METER.divide(100), "Millimeter", "mm");

  public static final Unit<Time> MINUTE = addUnit(USCustomary.MINUTE, "Minute", "min");
  public static final Unit<Time> HOUR = addUnit(USCustomary.HOUR, "Hour", "hr");
  public static final Unit<Time> SECOND = addUnit(MINUTE.divide(60), "Second", "sec");

  private PathUnits() {
    super();
  }

  @Override
  public String getName() {
    return SYSTEM_NAME;
  }

  public static SystemOfUnits getInstance() {
    return INSTANCE;
  }

  public static <U extends Unit<?>> U addUnit(U unit, String name, String label) {
    return addUnit(unit, name, label, true);
  }

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
}
