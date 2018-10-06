package edu.wpi.first.pathweaver;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class SaveManager {

  private static SaveManager instance;

  private final Set<Path> paths = new HashSet<>();

  private SaveManager() {
  }

  /**
   * Return the singleton instance of SaveManager. Tracks which files have been edited so the user can be prompted to
   * save them upon exit.
   * @return Singleton instance of SaveManager.
   */
  public static SaveManager getInstance() {
    if (instance == null) {
      instance = new SaveManager();
    }
    return instance;
  }

  public void addChange(Path path) {
    paths.add(path);
  }

  /**
   * Saves all Paths the user confirms are valid changes. Prompts the user for feedback.
   * @return True if application should close, false otherwise.
   */
  public boolean promptSaveAll() {
    for (Path path : paths) {
      Alert alert = new Alert(Alert.AlertType.NONE);
      alert.setTitle(path.getPathName() + " has been modified");
      alert.setContentText("Save " + path.getPathName() + "?");
      alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

      Optional<ButtonType> buttonType = alert.showAndWait();
      if (buttonType.isPresent()) {
        if (buttonType.get() == ButtonType.YES) {
          saveChange(path, false);
        } else if (buttonType.get() == ButtonType.CANCEL) {
          return false;
        }
      }
    }
    paths.clear(); // User has taken action on all paths
    return true;
  }

  /**
   * Saves the given path to the Project's Path directory. Removes the path from the set of modified paths.
   * @param path Path to save.
   */
  public void saveChange(Path path) {
    saveChange(path, true);
  }

  /**
   * Saves the given path to the Project's Path directory.
   * @param path Path to save.
   * @param remove Whether to remove Path from set of modified paths.
   */
  private void saveChange(Path path, boolean remove) {
    String pathDirectory = ProjectPreferences.getInstance().getDirectory() + "/Paths/";
    PathIOUtil.export(pathDirectory, path);
    if (remove) {
      paths.remove(path);
    }
  }

}