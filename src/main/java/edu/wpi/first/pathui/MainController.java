package edu.wpi.first.pathui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class MainController {
  @FXML private TreeView<String> autons;
  @FXML private TreeView<String> paths;
  @FXML private Pane pathDisplay;
  // Variable is auto generated as Pane name + Controller
  @FXML private PathDisplayController pathDisplayController; //NOPMD

  private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

  private String directory = "pathUI/";
  private String pathDirectory;
  private String autonDirectory;
  private final TreeItem<String> autonRoot = new TreeItem<String>("Autons");
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  private TreeItem<String> selected = null;

  @FXML
  private void initialize() {
    pathDirectory = directory + "Paths/";
    autonDirectory = directory + "Autons/";
    setupDrag(paths, false);
    setupDrag(autons, true);

    autons.setRoot(autonRoot);
    autons.getRoot().setExpanded(true);

    paths.setRoot(pathRoot);
    pathRoot.setExpanded(true);

    setupItemsInDirectory(pathDirectory, pathRoot);
    setupItemsInDirectory(autonDirectory, autonRoot);

    pathDisplayController.setPathDirectory(pathDirectory);

    setupClickablePaths();
    loadAllAutons();
  }

  private void loadAllAutons() {
    for (TreeItem<String> item : autonRoot.getChildren()) {
      loadAuton(autonDirectory, item.getValue(), item);
    }
  }

  private void saveAllAutons() {
    for (TreeItem<String> item : autonRoot.getChildren()) {
      saveAuton(autonDirectory, item.getValue(), item);
    }
  }

  @FXML
  private void createPath() {
    addChild(pathRoot, "Unnamed");
  }

  @FXML
  private void createAuton() {
    addChild(autonRoot, "Unnamed");
  }

  @FXML
  private void delete() {
    if (selected == null) {
      return;
    }
    TreeItem<String> root = getRoot(selected);
    if (selected == root) {
      System.out.println("clicked something dumb");
      return;
    }
    if (autonRoot == root) {
      if(selected.getParent().getParent() == null){
        deleteItem(autonDirectory,selected);
      }else{
        removePath(selected);
      }
    } else if (pathRoot == root) {
       deletePath(selected);
      saveAllAutons();
      loadAllAutons();
    }
  }

  @FXML
  private void keyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.DELETE) {
      delete();
    }
    if (event.getCode() == KeyCode.BACK_SPACE) {
      delete();
    }
  }
  private void deletePath(TreeItem<String> pathDelete){
    ArrayList<TreeItem<String>> deleteList = new ArrayList<>();
    for(TreeItem<String> auton : autonRoot.getChildren()){
      System.out.println("auton  " +auton);
      for (TreeItem<String> path : auton.getChildren()){
        System.out.println("path  "+path);
        if(path.getValue().equals(pathDelete.getValue())){
          System.out.println("same as " + pathDelete);
          //removePath(path);
          deleteList.add(path);
        }
      }
    }
    for(TreeItem<String> path : deleteList){
      removePath(path);
    }
    deleteItem(pathDirectory,pathDelete);
  }

  private void removePath(TreeItem<String> path){
    TreeItem<String> auton = path.getParent();
    auton.getChildren().remove(path);
    saveAuton(autonDirectory,auton.getValue(),auton);
  }


  private TreeItem<String> getRoot(TreeItem<String> item) {
    TreeItem<String> root = item;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return root;
  }


  private void setupClickablePaths() {
    paths.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              selected = newValue;
              if (newValue == pathRoot) {
                //pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                pathDisplayController.removeAllPath();
                pathDisplayController.addPath(pathDirectory, newValue.getValue());
              }

            });
    autons.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              selected = newValue;
              if (newValue == autonRoot) {
                //pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                pathDisplayController.removeAllPath();
                for (TreeItem<String> item : newValue.getChildren()) {
                  pathDisplayController.addPath(pathDirectory, item.getValue());

                }
              }
            });

  }

  private void setupDrag(TreeView<String> tree, boolean validDropTarget) {
    tree.setCellFactory(param -> new PathCell(validDropTarget));
    autons.setOnDragDropped(event -> {
      //simpler than communicating which was updated from the cells
      saveAllAutons();
      loadAllAutons();
    });
  }


  private void setupItemsInDirectory(String directory, TreeItem<String> root) {
    File folder = new File(directory);
    if (!folder.exists()) {
      folder.mkdir();
    }
    String[] listOfFiles = folder.list();
    for (String name : listOfFiles) {
      addChild(root, name);
    }
  }

  private void addChild(TreeItem<String> root, String name) {
    TreeItem<String> item = new TreeItem<>(name);
    root.getChildren().add(item);
  }

  private void loadAuton(String location, String filename, TreeItem<String> root) {
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

  private void saveAuton(String location, String filename, TreeItem<String> root) {
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

  private void deleteItem(String directory, TreeItem<String> item){

    File itemFile = new File(directory+item.getValue());
    if(itemFile.delete()){
      item.getParent().getChildren().remove(item);
      System.out.println("did delete");
    }else{
      System.out.println("didnt delete");
    }
  }



  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

