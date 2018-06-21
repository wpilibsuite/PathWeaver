package edu.wpi.first.pathui;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.geometry.Point2D;

public final class PathIOUtil {

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
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileLocation + path.getPathName()));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("X", "Y", "Tangent X", "Tangent Y", "Fixed Theta"));
    ) {
      Waypoint current = path.getStart();
      while (current != null) {
        double xPos = current.getX();
        double yPos = current.getY();
        double tangentX = current.getTangent().getX();
        double tangentY = current.getTangent().getY();
        csvPrinter.printRecord(xPos, yPos, tangentX, tangentY, current.isLockTangent());

        current = current.getNextWaypoint();

      }
      csvPrinter.flush();
    } catch (IOException except) {
      except.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Imports Path object from disk.
   *
   * @param fileLocation Folder with path file
   * @param fileName     Name of path file
   *
   * @return Path object saved in Path file
   */
  @SuppressWarnings("PMD.NcssCount")
  public static Path importPath(String fileLocation, String fileName) {
    try (
        Reader reader = Files.newBufferedReader(Paths.get(fileLocation + fileName));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim());
    ) {
      int count = 0;
      Point2D startPosition = null;
      Point2D startTangent = null;
      Path path = null;
      for (CSVRecord csvRecord : csvParser) {
        // Accessing values by Header names
        count++;
        Point2D position = new Point2D(
            Double.parseDouble(csvRecord.get("X")),
            Double.parseDouble(csvRecord.get("Y")));
        Point2D tangent = new Point2D(
            Double.parseDouble(csvRecord.get("Tangent X")),
            Double.parseDouble(csvRecord.get("Tangent Y")));
        boolean locked = Boolean.parseBoolean(csvRecord.get("Fixed Theta"));
        if (count == 1) {
          startPosition = position;
          startTangent = tangent;
        } else if (count == 2) {
          path = new Path(startPosition, position, startTangent, tangent, fileName);
        } else {
          path.addNewWaypoint(path.getEnd(), position, tangent, locked);
        }
      }
      return path;

    } catch (IOException except) {
      except.printStackTrace();
      return null;
    }

  }


}
