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

  public void saveResolution(double width, double height) {
    values.width = width;
    values.height = height;
    updatePrefs();
  }

  public void addProject(String path) {
    if (!values.recentProjects.contains(path)) {
      values.recentProjects.add(path);
      updatePrefs();
    }
  }

  public List<String> getRecentProjects() {
    return values.recentProjects;
  }

  public void setResolution(Stage primaryStage) {
    if (values.width != 0 && values.height != 0) {
      primaryStage.setWidth(values.width);
      primaryStage.setHeight(values.height);
    }
  }

  public class Values {
    public List<String> recentProjects;
    public double width;
    public double height;

    public Values() {
      recentProjects = new ArrayList<>();
    }
  }
}
