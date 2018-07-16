package edu.wpi.first.pathui;


import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.scene.image.Image;

import static edu.wpi.first.pathui.PathUnits.FOOT;

public class SaveUtils {


  public static void exportField(Field field, String savePath) throws IOException {
    FileWriter file = new FileWriter(savePath + "/Field/field.json");
    file.write(generateFieldJson(field).toString(2));
  }

//  public static void exportRobot(JSONObject object, String savePath) throws IOException {
//    FileWriter file = new FileWriter(savePath + "/Robot/robot.json");
//    file.write(object.toString(2));
//  }

  public static Field importField(String savePath) throws FileNotFoundException {
    FileReader reader = new FileReader(savePath + "/Field/field.json");
    String str = reader.toString();
    JSONObject temp = new JSONObject(str);
    return new Field(new Image(temp.getString("Image")),
        FOOT,//change me
        temp.getDouble("Real Width"),
        temp.getDouble("Real Length"),
        temp.getDouble("X"),
        temp.getDouble("Y"),
        temp.getDouble("Scale"));
  }

/*
public static void importRobot(String savePath) throws FileNotFoundException {
FileReader reader = new FileReader(savePath + "/Robot/robot.json");
String str = reader.toString();
robot = new JSONObject(str);
}
*/

  private static JSONObject generateFieldJson(Field newField) {
    JSONObject field = new JSONObject();
    field.put("Image", newField.getImage().getUrl());
    field.put("Unit", newField.getUnit().getName());
    field.put("Real Width", newField.getRealWidth());
    field.put("Real Length", newField.getRealLength());
    field.put("X", newField.getCoord().getX());
    field.put("Y", newField.getCoord().getY());
    field.put("Scale", newField.getScale());
    return field;
  }
}