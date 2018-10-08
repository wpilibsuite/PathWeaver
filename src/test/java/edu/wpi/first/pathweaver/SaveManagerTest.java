package edu.wpi.first.pathweaver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaveManagerTest {

  private TemporaryFolder folder;

  private String projectDirectory;
  private String pathDirectory;

  @BeforeEach
  public void initialize() throws IOException {
    folder = new TemporaryFolder();
    folder.create();
    projectDirectory = folder.getRoot().getAbsolutePath();
    pathDirectory = folder.newFolder("Paths").getAbsolutePath() + "/";
    ProjectPreferences.getInstance(projectDirectory);
  }

  @Test
  void saveAndLoadDefaultPath() {
    Path path = new Path("default");
    SaveManager.getInstance().saveChange(path);
    Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
    assertEquals(path, loadPath);
  }

  @Test
  void saveAndLoadWithAddedWaypoint() {
    Path path = new Path("default");
    path.addNewWaypoint(path.getStart().getSpline());
    SaveManager.getInstance().saveChange(path);
    Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
    assertEquals(path, loadPath);
  }

  @Test
  void addMultipleChangesAndSave() {
    Path pathOne = new Path("one");
    SaveManager.getInstance().addChange(pathOne);
    Path pathTwo = new Path("two");
    SaveManager.getInstance().addChange(pathTwo);
    Path pathThree = new Path("three");
    SaveManager.getInstance().addChange(pathThree);
    SaveManager.getInstance().saveAll();
    for (Path path : List.of(pathOne, pathTwo, pathThree)) {
      assertEquals(false, SaveManager.getInstance().hasChanges(path));
      Path loadPath = PathIOUtil.importPath(pathDirectory, path.getPathName());
      assertEquals(path, loadPath);
    }
  }
}
