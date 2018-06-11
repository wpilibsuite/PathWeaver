package edu.wpi.first.pathui;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class PathIOUtil {

  private PathIOUtil(){}


  /** Exports path object to csv file.
   *
   * @param filePath the directory and filename to write to
   * @param path Path object to save
   * @return true if successful file write was preformed
   */
  public static boolean export(String filePath, Path path) {
    try (
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath + ".path"));
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
      return false;
    }
    return true;
  }
}
