package edu.wpi.first.pathweaver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class SaveManager {

  private static SaveManager instance;

  private List<Path> paths = new ArrayList<>();

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
   * Saves all changed Paths without prompting the user for feedback.
   */
  public void saveAllChanges() {
    for (Path path : paths) {
      saveChange(path);
    }
  }

  /**
   * Saves all Paths the user confirms are valid changes. Prompts the user for feedback.
   * @return True if application should close, false otherwise.
   */
  public boolean promptSaveAll() {
    for (Path path : paths) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle(path.getPathName() + " has been modified");
      alert.setContentText("Save " + path.getPathName() + "?");
      ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
      ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
      ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
      alert.getButtonTypes().addAll(yesButton, noButton, cancelButton);
      AtomicBoolean cancel = new AtomicBoolean(false);
      alert.showAndWait().ifPresent(buttonType -> {
        if (buttonType == ButtonType.YES) {
          saveChange(path);
        } else if (buttonType == ButtonType.CANCEL) {
          cancel.set(true);
        }
      });
      if (cancel.get()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Saves the given path to the Project's Path directory.
   * @param path Path to save.
   */
  private void saveChange(Path path) {
    String pathDirectory = ProjectPreferences.getInstance().getDirectory() + "/Paths/";
    PathIOUtil.export(pathDirectory, path);
  }

}
