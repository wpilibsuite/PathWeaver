package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProjectPreferences {

  private static final String filename = "/pathweaver.project";

  private static ProjectPreferences instance;

  private final String directory;

  private Values values;

  private boolean defaults;

  private ProjectPreferences(String directory) {
    this.directory = directory;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(directory + filename));
      Gson gson = new Gson();
      values = gson.fromJson(reader, Values.class);
      defaults = false;
    } catch (FileNotFoundException e) {
      setDefaults();
    }
  }

  private void setDefaults() {
    defaults = true;
    values = new Values(0.2, 10.0, 60.0, 60.0, 2.0);
    updateValues();
  }

  private void updateValues() {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileWriter writer = new FileWriter(directory + filename);
      gson.toJson(values, writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setValues(Values values) {
    this.values = values;
    updateValues();
  }

  public Values getValues() {
    return values;
  }

  public String getDirectory() {
    return directory;
  }


  public static ProjectPreferences getInstance(String folder) {
    if (instance == null || !instance.directory.equals(folder)) {
      instance = new ProjectPreferences(folder);
    }
    return instance;
  }

  public static ProjectPreferences getInstance() {
    if (instance == null) {
      instance = new ProjectPreferences("PathWeaver");
    }
    return instance;

  }

  public static class Values {
    public double timeStep;
    public double maxVelocity;
    public double maxAcceleration;
    public double maxJerk;
    public double wheelBase;

    public Values(double timeStep, double maxVelocity, double maxAcceleration, double maxJerk,
                  double wheelBase) {
      this.timeStep = timeStep;
      this.maxVelocity = maxVelocity;
      this.maxAcceleration = maxAcceleration;
      this.maxJerk = maxJerk;
      this.wheelBase = wheelBase;
    }
  }
}
