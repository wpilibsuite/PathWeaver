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
import java.util.Collections;
import java.util.List;

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

  public void addProject(String path) {
    if (!values.recentProjects.contains(path)) {
      values.recentProjects.add(path);
      updatePrefs();
    }
  }

  public List<String> getRecentProjects() {
    return values.recentProjects;
  }

  public class Values {
    public List<String> recentProjects;

    public Values() {
      recentProjects = new ArrayList<>();
    }
  }
}
