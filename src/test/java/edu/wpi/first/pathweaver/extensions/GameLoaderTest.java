package edu.wpi.first.pathweaver.extensions;

import si.uom.impl.quantity.LengthAmount;

import edu.wpi.first.pathweaver.Game;
import edu.wpi.first.pathweaver.PathUnits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameLoaderTest {

	@Test
	public void testSimpleJson() {
		ExtensionLoader loader = new ExtensionLoader();
		String json = "{" + "\"" + ExtensionLoader.GAME_NAME_KEY + "\": \"TestGame\"," + "\""
				+ ExtensionLoader.FIELD_IMAGE_KEY + "\": \"img.png\"," + "\"" + ExtensionLoader.FIELD_CORNERS_KEY
				+ "\": {" + "\"" + ExtensionLoader.TOP_LEFT_KEY + "\": [0, 0]," + "\""
				+ ExtensionLoader.BOTTOM_RIGHT_KEY + "\": [4, 2]" + "}," + "\"" + ExtensionLoader.FIELD_SIZE_KEY
				+ "\": [16, 8]," + "\"" + ExtensionLoader.FIELD_UNITS_KEY + "\": \"feet\"" + "}";
		Game extension = loader.loadFromJsonString(name -> null, json);
		assertAll("Loading from JSON", () -> assertEquals("TestGame", extension.getName(), "Wrong name"),
				() -> assertEquals(0.25, extension.getField().getScale(), "Wrong scale"),
				() -> assertEquals(new LengthAmount(16, PathUnits.FOOT), extension.getField().getRealWidth(),
						"Wrong width"),
				() -> assertEquals(new LengthAmount(8, PathUnits.FOOT), extension.getField().getRealLength(),
						"Wrong length"));
	}

}
