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
    System.out.println("root + " + root);
    root.getChildren().addAll(
        new TreeItem<String>("Left Scoring Auton"),
        new TreeItem<String>("Left Defensive Auton"),
        new TreeItem<String>("Center Auton")
    );
    autons.setRoot(root);
    autons.getRoot().setExpanded(true);
    root.getChildren().get(0).getChildren().addAll(
        new TreeItem<String>("Left - Left Cube"),
        new TreeItem<String>("Left Cube - Switch"),
        new TreeItem<String>("Switch to Center Cube Pile")
    );
    root.getChildren().get(1).getChildren().addAll(
        new TreeItem<String>("Left - Defensive Position")
    );
    root.getChildren().get(2).getChildren().addAll(
        new TreeItem<String>("Right - Right Cube"),
        new TreeItem<String>("Right Cube - Switch"),
        new TreeItem<String>("Switch to Center Cube Pile")
    );


  }

}

