package edu.wpi.first.pathweaver.extensions;

import edu.wpi.first.pathweaver.Game;
import edu.wpi.first.pathweaver.PathUnits;

import org.junit.jupiter.api.Test;
import si.uom.quantity.impl.LengthAmount;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameLoaderTest {

	@Test
	public void testSimpleJson() {
		ExtensionLoader loader = new ExtensionLoader();
		String json = "{" + "\"" + ExtensionLoader.GAME_NAME_KEY + "\": \"TestGame1\"," + "\""
				+ ExtensionLoader.FIELD_IMAGE_KEY + "\": \"img.png\"," + "\"" + ExtensionLoader.FIELD_CORNERS_KEY
				+ "\": {" + "\"" + ExtensionLoader.TOP_LEFT_KEY + "\": [0, 0]," + "\""
				+ ExtensionLoader.BOTTOM_RIGHT_KEY + "\": [4, 2]" + "}," + "\"" + ExtensionLoader.FIELD_SIZE_KEY
				+ "\": [16, 8]," + "\"" + ExtensionLoader.FIELD_UNITS_KEY + "\": \"feet\"" + "}";
		Game extension = loader.loadFromJsonString(name -> null, json);
		assertAll("Loading from JSON", () -> assertEquals("TestGame1", extension.getName(), "Wrong name"),
				() -> assertEquals(0.25, extension.getField().getScale(), "Wrong scale"),
				() -> assertEquals(new LengthAmount(16.0, PathUnits.FOOT), extension.getField().getRealWidth(),
						"Wrong width"),
				() -> assertEquals(new LengthAmount(8.0, PathUnits.FOOT), extension.getField().getRealLength(),
						"Wrong length"));
	}

	@Test
	public void testConvertJson() {
		ExtensionLoader loader = new ExtensionLoader();
		String json = "{" + "\"" + ExtensionLoader.GAME_NAME_KEY + "\": \"TestGame2\"," + "\""
				+ ExtensionLoader.FIELD_IMAGE_KEY + "\": \"img.png\"," + "\"" + ExtensionLoader.FIELD_CORNERS_KEY
				+ "\": {" + "\"" + ExtensionLoader.TOP_LEFT_KEY + "\": [0, 0]," + "\""
				+ ExtensionLoader.BOTTOM_RIGHT_KEY + "\": [4, 2]" + "}," + "\"" + ExtensionLoader.FIELD_SIZE_KEY
				+ "\": [16, 8]," + "\"" + ExtensionLoader.FIELD_UNITS_KEY + "\": \"centimeters\"" + "}";
		Game extension = loader.loadFromJsonString(name -> null, json);
		assertAll("Loading from JSON", () -> assertEquals("TestGame2", extension.getName(), "Wrong name"),
				() -> assertEquals(0.25, extension.getField().getScale(), "Wrong scale"),
				() -> assertEquals(new LengthAmount(16.0, PathUnits.CENTIMETER), extension.getField().getRealWidth(),
						"Wrong width"),
				() -> assertEquals(new LengthAmount(8.0, PathUnits.CENTIMETER), extension.getField().getRealLength(),
						"Wrong length"));
	}

}
