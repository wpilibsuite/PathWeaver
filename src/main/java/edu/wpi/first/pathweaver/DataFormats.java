package edu.wpi.first.pathweaver;

import javafx.scene.input.DataFormat;

/**
 * Custom data formats for PathWeaver.
 */
public final class DataFormats {

	public static final String APP_PREFIX = "pathweaver";

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
