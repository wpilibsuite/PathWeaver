package edu.wpi.first.pathui;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;

public class MainController {
  @FXML private TreeView<String> autons;
  @FXML private TreeView<String> paths;
  @FXML private Pane pathDisplay;
  // Variable is auto generated as Pane name + Controller
  @FXML private PathDisplayController pathDisplayController; //NOPMD


  private String directory = "pathUI/";
  private String pathDirectory;
  private String autonDirectory;
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  @FXML
  private void initialize() {
    pathDirectory = directory + "Paths/";
    autonDirectory = directory + "Autons/";
    setupDrag(paths,false);
    setupDrag(autons,true);
    TreeItem<String> autonRoot = new TreeItem<String>("Autons");
    autonRoot.getChildren().addAll(
        new TreeItem<>("Left Scoring Auton"),
        new TreeItem<>("Left Defensive Auton"),
        new TreeItem<>("Center Auton")
    );
    autons.setRoot(autonRoot);
    autons.getRoot().setExpanded(true);
    autonRoot.getChildren().get(0).getChildren().addAll(
        new TreeItem<>("Left - Left Cube"),
        new TreeItem<>("Left Cube - Switch"),
        new TreeItem<>("Switch to Center Cube Pile")
    );
    autonRoot.getChildren().get(1).getChildren().addAll(
        new TreeItem<>("Left - Defensive Position")
    );
    autonRoot.getChildren().get(2).getChildren().addAll(
        new TreeItem<>("Right - Right Cube"),
        new TreeItem<>("Right Cube - Switch"),
        new TreeItem<>("Switch to Center Cube Pile")
    );
    paths.setRoot(pathRoot);
    pathRoot.setExpanded(true);
    setupItemsInDirectory(pathDirectory,pathRoot);
    setupItemsInDirectory(autonDirectory,autonRoot);
    setupClickablePaths();

  }

  private void setupClickablePaths() {
    paths.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue.getValue() == pathRoot.getValue()) {
                //pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                if (newValue != null) {
                  pathDisplayController.addPath(pathDirectory, newValue.getValue());
                }
                if (oldValue != null) {
                  pathDisplayController.removePath(pathDirectory, oldValue.getValue());
                }
              }
            });
  }

  private void setupDrag(TreeView<String> tree,boolean validDropTarget) {
    tree.setCellFactory(param -> new PathCell(validDropTarget));
  }

  private void setupItemsInDirectory(String directory, TreeItem<String> root) {
    File folder = new File(directory);
    File[] listOfFiles = folder.listFiles();
    for (File file : listOfFiles) {
      TreeItem<String> item = new TreeItem<>(file.getName());
      root.getChildren().add(item);    }
  }
  private void loadAuton(String location,String filename){
    File file = new File(location+filename);


  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

