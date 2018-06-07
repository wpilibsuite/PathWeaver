package edu.wpi.first.pathui;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class MainController {
  @FXML private TreeView<String> autons;
  @FXML private TreeView<String> paths;

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


  }

}

