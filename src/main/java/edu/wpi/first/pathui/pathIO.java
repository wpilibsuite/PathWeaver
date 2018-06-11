package edu.wpi.first.pathui;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class pathIO {
  static public boolean export(String filePath , Path path){
    try (
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath + ".path"));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("X", "Y", "Tangent X", "Tangent Y","Fixed Theta"));
    ) {
      Waypoint current = path.getStart();
      while (current != null) {
        double X = current.getX();
        double Y = current.getY();
        double tangentX = current.getTangent().getX();
        double tangentY = current.getTangent().getY();
        csvPrinter.printRecord(X, Y, tangentX, tangentY,current.isLockTangent());

        current = current.getNextWaypoint();

      }
      csvPrinter.flush();
    }catch (IOException except){
      return false;
    }
    return true;
  }
}
