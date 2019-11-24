package edu.wpi.first.pathweaver;

import edu.wpi.first.pathweaver.path.Path;
import edu.wpi.first.pathweaver.path.wpilib.WpilibPath;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SaveManagerTest {

  private String pathDirectory;

  @BeforeEach
  public void initialize(@TempDir java.nio.file.Path temp) throws IOException {
    Files.createDirectories(temp.resolve("Paths/"));
    String projectDirectory = temp.toAbsolutePath().toString();
    pathDirectory = temp.resolve("Paths/").toAbsolutePath().toString();
    ProjectPreferences.getInstance(projectDirectory);
  }

  @Test
  public void saveAndLoadDefaultPath() {
    Path path = new WpilibPath("default");
    SaveManager.getInstance().saveChange(path);
    Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
    assertEquals(path, loadPath, "Path loaded from disk doesn't match original");
  }

  @Test
  public void saveAndLoadWithAddedWaypoint() {
    Path path = new WpilibPath("default");
    path.addWaypoint(new Point2D(5.0, 5.0), path.getStart(), path.getEnd());
    SaveManager.getInstance().saveChange(path);
    Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
    assertEquals(path, loadPath, "Path loaded from disk with an added waypoint doesn't match original");
  }

  @Test
  public void addMultipleChangesAndSave() {
    List<Path> paths = getThreeDefaultPaths();
    paths.forEach(path -> SaveManager.getInstance().addChange(path));
    SaveManager.getInstance().saveAll();
    for (Path path : paths) {
      Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
      assertEquals(path, loadPath, "Path loaded from disk doesn't match original");
    }
  }

  @Test
  public void confirmPathsAreRemovedFromChangelist() {
    List<Path> paths = getThreeDefaultPaths();
    paths.forEach(path -> SaveManager.getInstance().addChange(path));
    SaveManager.getInstance().saveAll();
    for (Path path : paths) {
      assertFalse(SaveManager.getInstance().hasChanges(path),
          "Path change was not properly removed from SaveManager");
    }
  }

  private List<Path> getThreeDefaultPaths() {
    Path pathOne = new WpilibPath("one");
    Path pathTwo = new WpilibPath("two");
    Path pathThree = new WpilibPath("three");
    return List.of(pathOne, pathTwo, pathThree);
  }
}
