package edu.wpi.first.pathui;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.util.Arrays;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class pathIO {
  static private boolean export(String filePath , Path path) throws IOException{
    try (
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath+".path"));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("X", "Y", "TangentX", "TangentY"));
    ) {
      Waypoint current = path.getStart();
      while (current != null) {
        double X = current.getX();
        double Y = current.getY();
        double tangentX = current.getTangent().getX();
        double tangentY = current.getTangent().getY();
        current = current.getNextWaypoint();
        csvPrinter.printRecord(X, Y, tangentX, tangentY);
      }
      csvPrinter.flush();
    }

  }
}
