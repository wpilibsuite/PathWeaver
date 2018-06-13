package edu.wpi.first.pathui;

import javafx.scene.input.DataFormat;

/**
 * Custom data formats for PathUI.
 */
public final class DataFormats { //NOPMD

  public static final String APP_PREFIX = "pathui";

  /**
   * Data format for dragging waypoints.
   */
  public static final DataFormat WAYPOINT = new DataFormat(APP_PREFIX + "/waypoint");

  /**
   * Data format for dragging control vectors.
   */
  public static final DataFormat CONTROL_VECTOR = new DataFormat(APP_PREFIX + "/control-vector");

  /**
   * Data format for dragging spline.
   */
  public static final DataFormat SPLINE = new DataFormat(APP_PREFIX + "/spline");

  private DataFormats() {
    throw new UnsupportedOperationException("This is a utility class");
  }

}
