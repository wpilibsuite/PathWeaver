package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ProjectPreferences {

  private static final String filename = "/settings.json";

  private static ProjectPreferences instance;

  private final String folder;

  private Values values;

  private boolean defaults;

  private ProjectPreferences(String folder) {
    this.folder = folder;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(folder + filename));
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
      FileWriter writer = new FileWriter(folder + filename);
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


  public static ProjectPreferences getInstance(String folder) {
    if (instance == null || !instance.folder.equals(folder)) {
      instance = new ProjectPreferences(folder);
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
