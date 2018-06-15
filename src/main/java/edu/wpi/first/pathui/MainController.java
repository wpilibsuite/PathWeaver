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


  private String directory = "Paths/";
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  @FXML
  private void initialize() {
    TreeItem<String> root = new TreeItem<String>("Autons");
    root.getChildren().addAll(
        new TreeItem<>("Left Scoring Auton"),
        new TreeItem<>("Left Defensive Auton"),
        new TreeItem<>("Center Auton")
    );
    autons.setRoot(root);
    autons.getRoot().setExpanded(true);
    root.getChildren().get(0).getChildren().addAll(
        new TreeItem<>("Left - Left Cube"),
        new TreeItem<>("Left Cube - Switch"),
        new TreeItem<>("Switch to Center Cube Pile")
    );
    root.getChildren().get(1).getChildren().addAll(
        new TreeItem<>("Left - Defensive Position")
    );
    root.getChildren().get(2).getChildren().addAll(
        new TreeItem<>("Right - Right Cube"),
        new TreeItem<>("Right Cube - Switch"),
        new TreeItem<>("Switch to Center Cube Pile")
    );
    paths.setRoot(pathRoot);
    pathRoot.setExpanded(true);
    setupPathsInDirectory(directory);
    setupClickablePaths();
  }

  private void setupClickablePaths() {
    paths.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue.getValue() == pathRoot.getValue()) {
                pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                if (newValue != null) {
                  pathDisplayController.addPath(directory, newValue.getValue());
                }
                if (oldValue != null) {
                  pathDisplayController.removePath(directory, oldValue.getValue());
                }
              }
            });
  }

  private void setupPathTreeItem(String fileName) {
    TreeItem<String> item = new TreeItem<>(fileName);

    pathRoot.getChildren().add(item);

    //drag and click events
  }

  private void setupPathsInDirectory(String directory) {
    File folder = new File(directory);
    File[] listOfFiles = folder.listFiles();
    for (File file : listOfFiles) {
      setupPathTreeItem(file.getName());
    }
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

