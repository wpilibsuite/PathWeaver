package edu.wpi.first.pathui;


import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveUtils {

  private static JSONObject field = null;
  private static JSONObject robot = null;

  public static void exportField(JSONObject object, String savePath) throws IOException {
    FileWriter file = new FileWriter(savePath + "/Field/field.json");
    file.write(object.toString(2));
  }

  public static void exportRobot(JSONObject object, String savePath) throws IOException {
    FileWriter file = new FileWriter(savePath + "/Robot/robot.json");
    file.write(object.toString(2));
  }

  public static void importField(String savePath) throws FileNotFoundException {
    FileReader reader = new FileReader(savePath + "/Field/field.json");
    String str = reader.toString();
    field = new JSONObject(str);
  }

  public static void importRobot(String savePath) throws FileNotFoundException {
    FileReader reader = new FileReader(savePath + "/Robot/robot.json");
    String str = reader.toString();
    robot = new JSONObject(str);
  }
}