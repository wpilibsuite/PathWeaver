package edu.wpi.first.pathweaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectPreferences {

  private static final String FILENAME = "/pathweaver.project";

  private static ProjectPreferences instance;

  private final String directory;

  private Values values;

  private ProjectPreferences(String directory) {
    this.directory = directory;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(directory + FILENAME));
      Gson gson = new Gson();
      values = gson.fromJson(reader, Values.class);
    } catch (FileNotFoundException e) {
      setDefaults();
    }
  }

  private void setDefaults() {
    values = new Values(0.2, 10.0, 60.0, 60.0, 2.0);
    updateValues();
  }

  private void updateValues() {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      FileWriter writer = new FileWriter(directory + FILENAME);
      gson.toJson(values, writer);
      writer.close();
    } catch (IOException e) {
      Logger log = Logger.getLogger(getClass().getName());
      log.log(Level.WARNING, "Couldn't update Project Preferences", e);
    }
  }

  /**
   * Sets the preferences for the current project.
   * @param values Values to set for preferences.
   */
  public void setValues(Values values) {
    this.values = values;
    updateValues();
  }

  public String getDirectory() {
    return directory;
  }


  /**
   * Return the singleton instance of ProjectPreferences for a given project directory.
   * @param folder Path to project folder.
   * @return Singleton instance of ProjectPreferences.
   */
  public static ProjectPreferences getInstance(String folder) {
    if (instance == null || !instance.directory.equals(folder)) {
      instance = new ProjectPreferences(folder);
    }
    return instance;
  }

  /**
   * Returns the singleton instance of ProjectPreferences for the previously requested directory
   * or the default directory.
   * @return Singleton instance of ProjectPreferences.
   */
  public static ProjectPreferences getInstance() {
    return instance;
  }

  public static boolean projectExists(String folder) {
    return new File(folder + FILENAME).exists();
  }

  public static class Values {
    private final double timeStep;
    private final double maxVelocity;
    private final double maxAcceleration;
    private final double maxJerk;
    private final double wheelBase;
    private final String game;

    /**
     * Constructor for Values of ProjectPreferences.
     * @param timeStep        The time delta between points (in seconds)
     * @param maxVelocity     The maximum velocity the body is capable of travelling at
     * @param maxAcceleration The maximum acceleration to use
     * @param maxJerk         The maximum jerk (acceleration per second) to use
     * @param wheelBase       The width between the individual sides of the drivebase
     */
    public Values(double timeStep, double maxVelocity, double maxAcceleration, double maxJerk,
                  double wheelBase) {
      this.timeStep = timeStep;
      this.maxVelocity = maxVelocity;
      this.maxAcceleration = maxAcceleration;
      this.maxJerk = maxJerk;
      this.wheelBase = wheelBase;
      this.game  = "POWERUP";
    }

    public double getTimeStep() {
      return timeStep;
    }

    public double getMaxVelocity() {
      return maxVelocity;
    }

    public double getMaxAcceleration() {
      return maxAcceleration;
    }

    public double getMaxJerk() {
      return maxJerk;
    }

    public double getWheelBase() {
      return wheelBase;
    }
  }
}
