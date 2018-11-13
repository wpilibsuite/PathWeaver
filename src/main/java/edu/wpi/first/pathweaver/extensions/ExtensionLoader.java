package edu.wpi.first.pathweaver.extensions;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import edu.wpi.first.pathweaver.DuplicateGameException;
import edu.wpi.first.pathweaver.Field;
import edu.wpi.first.pathweaver.Game;
import edu.wpi.first.pathweaver.PathUnits;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

/**
 * Loads game extensions. Extensions are defined by a JSON file and an image file for the field.
 *
 * <h3>JSON structure</h3>
 * <pre>{@code
 * {
 *   "game": "game name",
 *   "field-image": "relative/path/to/img.png",
 *   "field-corners": {
 *     "top-left": [x, y],
 *     "bottom-right": [x, y]
 *   },
 *   "field-size": [width, length],
 *   "field-unit": "unit name"
 * }
 * }</pre>
 * <br>The path to the field image is relative to the JSON file.
 * <br>The field corners are the X and Y coordinates of the top-left and bottom-right pixels defining the rectangular
 * boundary of the playable area in the field image. Non-rectangular playing areas are not supported.
 * <br>The field size is the width and length of the playable area of the field in the provided units.
 * <br>The field units are not case-sensitive and can be one of:
 * <table>
 * <tr><th>Unit</th><th>Accepted Values</th></tr>
 * <tr><td>Meter</td><td>"meter", "meters", "m"</td></tr>
 * <tr><td>Centimeter</td><td>"centimeter", "centimeters", "cm"</td></tr>
 * <tr><td>Millimeter</td><td>"millimeter", "millimeters", "mm"</td></tr>
 * <tr><td>Inch</td><td>"inch", "inches", "in"</td></tr>
 * <tr><td>Foot</td><td>"foot", "feet", "ft"</td></tr>
 * <tr><td>Yard</td><td>"yard", "yards", "yd"</td></tr>
 * <tr><td>Mile</td><td>"mile", "miles", "mi"</td></tr>
 * </table>
 */
public final class ExtensionLoader {

  public static final String GAME_NAME_KEY = "game";
  public static final String FIELD_IMAGE_KEY = "field-image";
  public static final String FIELD_CORNERS_KEY = "field-corners";
  public static final String TOP_LEFT_KEY = "top-left";
  public static final String BOTTOM_RIGHT_KEY = "bottom-right";
  public static final String FIELD_SIZE_KEY = "field-size";
  public static final String FIELD_UNITS_KEY = "field-unit";

  private static final Logger log = Logger.getLogger(ExtensionLoader.class.getName());

  /**
   * Loads a game + field image extension from a JSON file.
   *
   * @param jsonFile the JSON extension file.
   *
   * @throws IOException if the file could not be read
   */
  public Game loadFromJsonFile(Path jsonFile) throws IOException, DuplicateGameException {
    if (!jsonFile.getFileName().toString().endsWith(".json")) {
      throw new IllegalArgumentException("Not a JSON file: " + jsonFile);
    }
    String json = Files.readString(jsonFile, StandardCharsets.UTF_8);
    return loadFromJsonString(fileName -> loadImage(jsonFile.getParent(), fileName), json);
  }

  private static Image loadImage(Path dir, String fileName) {
    String url = "file://" + dir.resolve(fileName).toAbsolutePath().toString();
    return new Image(url);
  }

  /**
   * Loads a game from a directory. If there are multiple JSON files in the directory, one of them <i>must</i> be named
   * "game.json". If not, an IllegalArgumentException is thrown. If only one JSON file is in the directory,
   * it may have any name (but must still end with ".json").
   *
   * <p>This is not recursive: only the files contained in the directory are looked at.
   *
   * @param dir the directory to load a game from
   *
   * @return the game object defined in the directory
   *
   * @throws IOException              if files in the directory could not be read
   * @throws DuplicateGameException   if a game already exists with the name specified in the game JSON
   * @throws IllegalArgumentException if there are no JSON files in the directory
   * @throws IllegalArgumentException if there are multiple JSON files in the directory and none named "game.json"
   */
  public Game loadFromDir(Path dir) throws IOException, DuplicateGameException {
    List<Path> possibleJsonFiles = Files.list(dir)
        .filter(path -> path.toString().endsWith(".json"))
        .collect(Collectors.toList());
    if (possibleJsonFiles.isEmpty()) {
      throw new IllegalArgumentException("No JSON files present in the root directory");
    }

    Optional<Path> gameJson = possibleJsonFiles
        .stream()
        .filter(path -> path.getFileName().toString().equals("game.json"))
        .findFirst();
    if (gameJson.isPresent()) {
      return loadFromJsonFile(gameJson.get());
    } else if (possibleJsonFiles.size() == 1) {
      return loadFromJsonFile(possibleJsonFiles.get(0));
    } else {
      throw new IllegalArgumentException("Cannot determine the JSON file to use");
    }
  }

  /**
   * Loads a game from a zip file. The zip file is expected to have the same structure as a valid directory to load
   * from.
   *
   * @param zipFile the extension zip file to load
   *
   * @return the game object defined in the zip file
   *
   * @throws IOException              if files in the zip file could not be read
   * @throws DuplicateGameException   if a game already exists with the name specified in the game JSON
   * @throws IllegalArgumentException if there are no JSON files in the zip file
   * @throws IllegalArgumentException if there are multiple JSON files in the zip file and none named "game.json"
   * @see #loadFromDir(Path)
   */
  public Game loadFromZip(Path zipFile) throws IOException, DuplicateGameException {
    Path dir = Files.createTempDirectory("pathweaver-extension-" + zipFile.getFileName());

    try (ZipFile zip = new ZipFile(zipFile.toFile())) {
      for (ZipEntry entry : Collections.list(zip.entries())) {
        if (entry.isDirectory()) {
          Files.createDirectories(dir.resolve(entry.getName()));
        } else {
          Path dst = dir.resolve(entry.getName());
          try (var in = zip.getInputStream(entry); var out = Files.newOutputStream(dst)) {
            in.transferTo(out);
          }
        }
      }
    }

    try {
      return loadFromDir(dir);
    } finally {
      // Make sure to clean up the temp files, even if an exception is thrown when attempting to load from the temp dir
      try {
        deleteDir(dir);
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not delete temp directory " + dir, e);
      }
    }
  }

  private static void deleteDir(Path dir) throws IOException {
    for (Path child : Files.list(dir).collect(Collectors.toList())) {
      if (Files.isDirectory(child)) {
        deleteDir(child);
      } else {
        Files.delete(child);
      }
    }
    Files.delete(dir);
  }

  /**
   * Loads a game from a JSON string.
   *
   * @param imageProvider supplies an {@code Image} object given the name of an image file
   * @param json          the JSON string to parse
   *
   * @return the game object defined by the JSON text
   *
   * @throws DuplicateGameException if a game already exists with the given name
   */
  public Game loadFromJsonString(Function<String, Image> imageProvider, String json) throws DuplicateGameException {
    return new GsonBuilder()
        .registerTypeAdapter(Game.class, new ExtensionJsonDeserializer(imageProvider))
        .create()
        .fromJson(json, Game.class);
  }

  private static Point2D jsonArrayToPoint(JsonArray array) {
    if (array.size() != 2) {
      throw new JsonParseException("Expected two elements [x, y]; got " + array);
    }
    return new Point2D(
        array.get(0).getAsDouble(),
        array.get(1).getAsDouble()
    );
  }

  private static final class ExtensionJsonDeserializer implements JsonDeserializer<Game> {

    private final Function<String, Image> imageProvider;

    private ExtensionJsonDeserializer(Function<String, Image> imageProvider) {
      this.imageProvider = imageProvider;
    }

    @Override
    public Game deserialize(JsonElement element, Type t, JsonDeserializationContext c) throws JsonParseException {
      var jsonObject = element.getAsJsonObject();
      String gameName = jsonObject
          .get(GAME_NAME_KEY)
          .getAsString();

      String imagePath = jsonObject
          .get(FIELD_IMAGE_KEY)
          .getAsString();

      var corners = jsonObject.get(FIELD_CORNERS_KEY).getAsJsonObject();
      Point2D topLeftPoint = jsonArrayToPoint(corners.get(TOP_LEFT_KEY).getAsJsonArray());
      Point2D bottomRightPoint = jsonArrayToPoint(corners.get(BOTTOM_RIGHT_KEY).getAsJsonArray());

      Point2D fieldSize = jsonArrayToPoint(jsonObject.get(FIELD_SIZE_KEY).getAsJsonArray());
      String fieldUnit = jsonObject.get(FIELD_UNITS_KEY).getAsString();

      Field field = new Field(
          imageProvider.apply(imagePath),
          PathUnits.getInstance().length(fieldUnit),
          fieldSize.getX(),
          fieldSize.getY(),
          topLeftPoint.getX(),
          topLeftPoint.getY(),
          bottomRightPoint.getX(),
          bottomRightPoint.getY()
      );

      return Game.create(gameName, field);
    }
  }
}
