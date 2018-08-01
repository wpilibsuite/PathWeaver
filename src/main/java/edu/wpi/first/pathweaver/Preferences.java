package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Preferences {

  private static final String path = "PathWeaver/settings.json";

  private static Preferences instance;

  private Values prefs;

  private boolean defaults;

  private Preferences() {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      Gson gson = new Gson();
      prefs = gson.fromJson(reader, Values.class);
      defaults = false;
    } catch (FileNotFoundException e) {
      setDefaults();
    }
  }

  private void setDefaults() {
    defaults = true;
    prefs = new Values();
    prefs.timeStep = 0.2;
    prefs.maxVelocity = 10.0;
    prefs.maxAcceleration = 60.0;
    prefs.maxJerk = 60.0;
    prefs.wheelBase = 2.0;
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileWriter writer = new FileWriter(path);
      gson.toJson(prefs, writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private JsonObject getValue() {
    return null;
  }

  public static Preferences getInstance() {
    if (instance == null) {
      instance = new Preferences();
    }
    return instance;
  }

  public enum Option {
    TimeStep("timeStep"),
    MaxVelocity("maxVelocity"),
    MaxAcceleration("maxAcceleration"),
    MaxJerk("maxJerk"),
    WheelBase("wheelBase");

    private String key;

    Option(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }

  private class Values {
    public double timeStep;
    public double maxVelocity;
    public double maxAcceleration;
    public double maxJerk;
    public double wheelBase;
  }
}
