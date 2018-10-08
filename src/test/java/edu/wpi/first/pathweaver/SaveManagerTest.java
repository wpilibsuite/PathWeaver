package edu.wpi.first.pathweaver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SaveManagerTest {

  private String pathDirectory;

  @BeforeEach
  public void initialize() throws IOException {
    TemporaryFolder folder = new TemporaryFolder();
    folder.create();
    String projectDirectory = folder.getRoot().getAbsolutePath();
    pathDirectory = folder.newFolder("Paths").getAbsolutePath() + "/";
    ProjectPreferences.getInstance(projectDirectory);
  }

  @Test
  public void saveAndLoadDefaultPath() {
    Path path = new Path("default");
    SaveManager.getInstance().saveChange(path);
    Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
    assertEquals(path, loadPath, "Path loaded from disk doesn't match original");
  }

  @Test
  public void saveAndLoadWithAddedWaypoint() {
    Path path = new Path("default");
    path.addNewWaypoint(path.getStart().getSpline());
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
    Path pathOne = new Path("one");
    Path pathTwo = new Path("two");
    Path pathThree = new Path("three");
    return List.of(pathOne, pathTwo, pathThree);
  }
}
