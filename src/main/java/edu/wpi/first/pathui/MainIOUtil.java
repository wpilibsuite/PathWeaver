package edu.wpi.first.pathui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.TreeItem;

public final class MainIOUtil {
  private static final Logger LOGGER = Logger.getLogger(MainIOUtil.class.getName());

  private MainIOUtil() {
  }

  public static void setupItemsInDirectory(String directory, TreeItem<String> root) {
    File folder = new File(directory);
    if (!folder.exists()) {
      folder.mkdir();
    }
    String[] listOfFiles = folder.list();
    for (String name : listOfFiles) {
      addChild(root, name);
    }
  }

  public static void addChild(TreeItem<String> root, String name) {
    TreeItem<String> item = new TreeItem<>(name);
    root.getChildren().add(item);
  }

  public static void deleteItem(String directory, TreeItem<String> item) {

    File itemFile = new File(directory + item.getValue());
    if (itemFile.delete()) {
      item.getParent().getChildren().remove(item);
    } else {
      LOGGER.log(Level.WARNING, "Could not delete file: " + itemFile.getAbsolutePath());
    }
  }

  public static void loadAuton(String location, String filename, TreeItem<String> root) {
    BufferedReader reader;
    root.getChildren().clear();
    try {
      reader = new BufferedReader(new FileReader(location + filename));
      String line = reader.readLine();
      while (line != null) {
        addChild(root, line);
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not load auton file", e);
    }
  }

  public static void saveAuton(String location, String filename, TreeItem<String> root) {
    BufferedWriter writer;
    try {
      writer = new BufferedWriter(new FileWriter(location + filename));
      for (TreeItem<String> item : root.getChildren()) {
        writer.write(item.getValue());
        writer.newLine();
      }

      writer.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not save auton file", e);
    }
  }


}
