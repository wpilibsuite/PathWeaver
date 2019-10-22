package edu.wpi.first.pathweaver.spline;

import javafx.scene.Group;

import java.nio.file.Path;
@SuppressWarnings("PMD.NcssCount")

/**
 * Representation of a connection between points. Adds a JavaFX node that shows the connection.
 */
public interface Spline {
  void update();

  void addToGroup(Group splineGroup, double scaleFactor);

  void enableSubchildSelector(int i);

  void removeFromGroup(Group splineGroup);

  boolean writeToFile(Path path);
}
