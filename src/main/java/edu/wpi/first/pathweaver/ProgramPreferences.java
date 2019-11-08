package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgramPreferences {
  private static final String FILE_NAME = "pathweaver.json";
  private static final ProgramPreferences INSTANCE = new ProgramPreferences();

  private Values values;
  private final String directory;

  private ProgramPreferences() {
    directory = System.getProperty("user.home") + "/PathWeaver/";

    try(BufferedReader prefs = Files.newBufferedReader(Paths.get(directory, FILE_NAME))) {
      Gson gson = new GsonBuilder().serializeNulls().create();
      values = gson.fromJson(prefs, Values.class);
    } catch (IOException e) {
      values = new Values();
      updatePrefs();
    } catch (JsonParseException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Preferences import error");
      alert.setContentText(
              "Preferences have been reset due to file corruption. You may reimport your projects with the 'Import Project' button");
      ((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
      alert.show();

      values = new Values();
      updatePrefs();
    }
  }

  /**
   * Return the singleton instance of ProgramPreferences.
   * @return Singleton instance of ProgramPreferences.
   */
  public static ProgramPreferences getInstance() {
    return INSTANCE;
  }

  private void updatePrefs() {
    Path output = Paths.get(directory, FILE_NAME);
    try {
      Files.createDirectories(output);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      try(BufferedWriter writer = Files.newBufferedWriter(output)) {
        gson.toJson(values, writer);
      }
    } catch (IOException e) {
      Logger log = Logger.getLogger(getClass().getName());
      log.log(Level.WARNING, "couldn't update program preferences", e);
    }
  }

  /**
   * Adds a project to the beginning of the list of recent projects.
   * @param path Path to the project.
   */
  public void addProject(String path) {
    values.addProject(path);
    updatePrefs();
  }

  /**
   * Returns a list of paths of recent projects.
   * @return List of paths of recent projects.
   */
  public List<String> getRecentProjects() {
    return values.getRecentProjects();
  }

  /**
   * Sets the size, position, and maximized values for the primaryStage based upon previous preferences.
   * @param primaryStage The Stage to set the values for.
   */
  public void setSizeAndPosition(Stage primaryStage) {
    if (values.getWidth() == 0 || values.getHeight() == 0 || values.getPosX() == 0 || values.getPosY() == 0) {
      primaryStage.setWidth(1024);
      primaryStage.setHeight(768);
    } else {
      primaryStage.setWidth(values.getWidth());
      primaryStage.setHeight(values.getHeight());
      primaryStage.setX(values.getPosX());
      primaryStage.setY(values.getPosY());
      primaryStage.setMaximized(values.isMaximized());
    }
  }

  /**
   * Saves the current size, position and maximized values to preferences file.
   * @param primaryStage The stage to save size, position, and maximized values for.
   */
  public void saveSizeAndPosition(Stage primaryStage) {
    values.setSizeAndPosition(primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.getX(),
        primaryStage.getY(), primaryStage.isMaximized());
    updatePrefs();
  }

  public void removeProject(String folder) {
    values.removeProject(folder);
    updatePrefs();
  }

  private class Values {
    private List<String> recentProjects;
    private double width;
    private double height;
    private double posX;
    private double posY;
    private boolean maximized;

    public List<String> getRecentProjects() {
      if (recentProjects == null) {
        recentProjects = new ArrayList<>();
      }
      return recentProjects;
    }

    public double getWidth() {
      return width;
    }

    public double getHeight() {
      return height;
    }

    public double getPosX() {
      return posX;
    }

    public double getPosY() {
      return posY;
    }

    public boolean isMaximized() {
      return maximized;
    }

    public Values() {
    }

    public void setSizeAndPosition(double width, double height, double posX, double posY, boolean maximized) {
      this.width = width;
      this.height = height;
      this.posX = posX;
      this.posY = posY;
      this.maximized = maximized;
    }

    public void addProject(String path) {
      if (recentProjects == null) {
        recentProjects = new ArrayList<>();
      }
      recentProjects.remove(path);
      recentProjects.add(0, path);
    }

    public void removeProject(String path) {
      recentProjects.remove(path);
    }
  }
}
