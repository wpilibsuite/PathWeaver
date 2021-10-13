package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import edu.wpi.first.pathweaver.path.Path;
import edu.wpi.first.pathweaver.path.wpilib.WpilibPath;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Point2D;

public final class PathIOUtil {
  private static final Logger LOGGER = Logger.getLogger(PathIOUtil.class.getName());

  private PathIOUtil() {
  }


  /**
   * Exports path object to csv file.
   *
   * @param fileLocation the directory and filename to write to
   * @param path         Path object to save
   *
   * @return true if successful file write was preformed
   */

  public static boolean export(String fileLocation, Path path) {
    try (
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileLocation, path.getPathName()))
    ) {
      Config config = new Config(path);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String contents = gson.toJson(config);
      writer.write(contents);
    } catch (IOException except) {
      LOGGER.log(Level.WARNING, "Could not save Path file", except);
      return false;
    }
    return true;
  }

  public static Path importPath(String fileLocation, String fileName) {
    Config config = importV1Path(fileLocation, fileName, false);

    // We were able to read the file as the JSON version. Just return it.
    if (config != null) {
      return config.toPath(fileName);
    }

    // Try to upconvert the old CSV style config file.
    LOGGER.log(Level.INFO, "Could not read Path file. Will try to parse it as a legacy CSV file");
    Config legacyPoints = importV0Path(fileLocation, fileName);
    String newFileName = fileName + ".json";
    export(fileLocation, legacyPoints.toPath(newFileName));
    config = importV1Path(fileLocation, newFileName, true);
    if (config == null) {
      LOGGER.log(Level.WARNING, "Could not read it as a CSV file either");
      return null;
    }

    // We were able to convert it. Delete the old file, and return the json version
    try {
      Files.delete(Paths.get(fileLocation, fileName));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not delete old file");
    }

    return config.toPath(newFileName);
  }

  private static class ConfigPoint {
    private final double x;
    private final double y;
    private final double tangentX;
    private final double tangentY;
    private final boolean fixedTheta;
    private final boolean reversed;
    private final String name;

    public ConfigPoint(Waypoint waypoint, double height) {

      x = waypoint.getX();
      y = height + waypoint.getY();
      tangentX = waypoint.getTangentX();
      tangentY = waypoint.getTangentY();
      fixedTheta = waypoint.isLockTangent();
      reversed = waypoint.isReversed();
      name = waypoint.getName();
    }

    public ConfigPoint(CSVRecord csvRecord, double height) {
      x = Double.parseDouble(csvRecord.get("X"));
      y = height + Double.parseDouble(csvRecord.get("Y"));
      tangentX = Double.parseDouble(csvRecord.get("Tangent X"));
      tangentY = Double.parseDouble(csvRecord.get("Tangent Y"));
      fixedTheta = Boolean.parseBoolean(csvRecord.get("Fixed Theta"));
      reversed = Boolean.parseBoolean(csvRecord.get("Reversed"));
      if (csvRecord.isMapped("Name")) {
        name = csvRecord.get("Name");
      } else {
        name = "";
      }
    }

    public Waypoint toWaypoint(double height) {
      Point2D position = new Point2D(x,  y - height);
      Point2D tangent = new Point2D(tangentX, tangentY);
      Waypoint waypoint = new Waypoint(position, tangent, fixedTheta, reversed);
      waypoint.setName(name);

      return waypoint;
    }

    @Override
    public String toString() {
      return "ConfigPoint{" +
              "x=" + x +
              ", y=" + y +
              ", tangentX=" + tangentX +
              ", tangentY=" + tangentY +
              ", fixedTheta=" + fixedTheta +
              ", reversed=" + reversed +
              ", name='" + name + '\'' +
              '}';
    }
  }


  private static class Config {
    private static final String VERSION = "v1";

    public final String version = VERSION;
    public final String lengthUnit = ProjectPreferences.getInstance().getValues().getLengthUnit().getName();
    public final List<ConfigPoint> points = new ArrayList<>();

    public Config() {
    }

    public Config(CSVParser csvParser) {
      double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();
      for (CSVRecord csvRecord : csvParser) {
        points.add(new ConfigPoint(csvRecord, height));
      }
    }


    public Config(Path path) {
      double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();

      for (Waypoint wp : path.getWaypoints()) {
        points.add(new ConfigPoint(wp, height));
      }
    }

    public Path toPath(String fileName) {
      double height = ProjectPreferences.getInstance().getField().getRealLength().getValue().doubleValue();
      ArrayList<Waypoint> waypoints = new ArrayList<>();
      for (ConfigPoint point : points) {
        waypoints.add(point.toWaypoint(height));
      }

      return new WpilibPath(waypoints, fileName);
    }

    @Override
    public String toString() {
      return "Config{" +
              "points=" + points +
              '}';
    }
  }


  /**
   * Imports Path object from disk.
   *
   * @param fileLocation Folder with path file
   * @param fileName     Name of path file
   *
   * @return Path object saved in Path file
   */
  private static Config importV0Path(String fileLocation, String fileName) {
    try(Reader reader = Files.newBufferedReader(java.nio.file.Path.of(fileLocation, fileName));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim())) {
      return new Config(csvParser);
    } catch (IOException except) {
      LOGGER.log(Level.WARNING, "Could not read Path file", except);
      return null;
    }
  }

  private static Config importV1Path(String fileLocation, String fileName, boolean logException) {
    try (Reader reader = Files.newBufferedReader(java.nio.file.Path.of(fileLocation, fileName))) {
      return new Gson().fromJson(reader, Config.class);
    } catch (IOException ex) {
      LOGGER.log(Level.WARNING, "Could not read Path file", ex);
      return null;
    }catch (JsonParseException ex) {
      if (logException) {
        LOGGER.log(Level.WARNING, "Could not parse json", ex);
      } else {
        LOGGER.log(Level.WARNING, "Could not parse json");
      }
      return null;
    }
  }
}
