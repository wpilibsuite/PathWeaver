package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.stage.Stage;

public class ProgramPreferences {
  private static ProgramPreferences instance;
  private Values values;

  private final String directory;
  private static final String FILENAME = "pathweaver.json";

  private ProgramPreferences() {
    directory = System.getProperty("user.home") + "/PathWeaver/";
    File folder = new File(directory);
    if (!folder.exists()) {
      folder.mkdir();
    }
    try {
      BufferedReader prefs = new BufferedReader(new FileReader(directory + FILENAME));
      Gson gson = new GsonBuilder().serializeNulls().create();
      values = gson.fromJson(prefs, Values.class);
    } catch (FileNotFoundException e) {
      values = new Values();
      updatePrefs();
    }
    if (values.recentProjects == null) {
      values.recentProjects = new ArrayList<>();
    }
  }

  /**
   * Return the singleton instance of ProgramPreferences.
   * @return Singleton instance of ProgramPreferences.
   */
  public static ProgramPreferences getInstance() {
    if (instance == null) {
      instance = new ProgramPreferences();
    }
    return instance;
  }

  private void updatePrefs() {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileWriter writer = new FileWriter(directory + FILENAME);
      gson.toJson(values, writer);
      writer.close();
    } catch (IOException e) {
      Logger log = LogManager.getLogManager().getLogger(getClass().getName());
      log.log(Level.WARNING, e.getMessage());
    }
  }

  /**
   * Adds a project to the beginning of the list of recent projects.
   * @param path Path to the project.
   */
  public void addProject(String path) {
    values.recentProjects.remove(path);
    values.recentProjects.add(0, path);
    updatePrefs();
  }

  /**
   * Returns a list of paths of recent projects.
   * @return List of paths of recent projects.
   */
  public List<String> getRecentProjects() {
    return values.recentProjects;
  }

  /**
   * Sets the size, position, and maximized values for the primaryStage based upon previous preferences.
   * @param primaryStage The Stage to set the values for.
   */
  public void setSizeAndPosition(Stage primaryStage) {
    if (values.width != 0 && values.height != 0 && values.posX != 0 && values.posY != 0) {
      primaryStage.setWidth(values.width);
      primaryStage.setHeight(values.height);
      primaryStage.setX(values.posX);
      primaryStage.setY(values.posY);
      primaryStage.setMaximized(values.maximized);
    }
  }

  /**
   * Saves the current size, position and maximized values to preferences file.
   * @param primaryStage The stage to save size, position, and maximized values for.
   */
  public void saveSizeAndPosition(Stage primaryStage) {
    values.width = primaryStage.getWidth();
    values.height = primaryStage.getHeight();
    values.posX = primaryStage.getX();
    values.posY = primaryStage.getY();
    values.maximized = primaryStage.isMaximized();
    updatePrefs();
  }

  private class Values {
    public List<String> recentProjects;
    public double width;
    public double height;
    public double posX;
    public double posY;
    public boolean maximized;

    public Values() {
    }
  }
}
