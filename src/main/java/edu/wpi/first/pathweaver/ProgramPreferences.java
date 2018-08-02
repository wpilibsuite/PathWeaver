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

import javafx.stage.Stage;

public class ProgramPreferences {
  private static ProgramPreferences instance;
  private Values values;

  private String directory;
  private String fileName = "pathweaver.json";

  private ProgramPreferences() {
    directory = System.getProperty("user.home") + "/PathWeaver/";
    File folder = new File(directory);
    if (!folder.exists()) {
      folder.mkdir();
    }
    try {
      BufferedReader prefs = new BufferedReader(new FileReader(directory + fileName));
      Gson gson = new GsonBuilder().serializeNulls().create();
      values = gson.fromJson(prefs, Values.class);
      if (values.recentProjects == null) {
        values.recentProjects = new ArrayList<>();
      }
    } catch (FileNotFoundException e) {
      values = new Values();
      updatePrefs();
    }
  }

  public static ProgramPreferences getInstance() {
    if (instance == null) {
      instance = new ProgramPreferences();
    }
    return instance;
  }

  private void updatePrefs() {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileWriter writer = new FileWriter(directory + fileName);
      gson.toJson(values, writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addProject(String path) {
    values.recentProjects.remove(path);
    values.recentProjects.add(0, path);
    updatePrefs();
  }

  public List<String> getRecentProjects() {
    return values.recentProjects;
  }

  public void setSizeAndPosition(Stage primaryStage) {
    if (values.width != 0 && values.height != 0 && values.posX != 0 && values.posY != 0) {
      primaryStage.setWidth(values.width);
      primaryStage.setHeight(values.height);
      primaryStage.setX(values.posX);
      primaryStage.setY(values.posY);
    }
  }

  public void saveSizeAndPosition(Stage primaryStage) {
    values.width = primaryStage.getWidth();
    values.height = primaryStage.getHeight();
    values.posX = primaryStage.getX();
    values.posY = primaryStage.getY();
    updatePrefs();
  }

  private class Values {
    public List<String> recentProjects;
    public double width;
    public double height;
    public double posX;
    public double posY;

    public Values() {
    }
  }
}
