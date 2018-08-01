package edu.wpi.first.pathweaver;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class CreateProjectController {

  @FXML
  VBox vBox;
  @FXML
  TextField directory;
  @FXML
  Button browse;
  @FXML
  TextField timeStep;
  @FXML
  TextField maxVelocity;
  @FXML
  TextField maxAcceleration;
  @FXML
  TextField maxJerk;
  @FXML
  TextField wheelBase;
  @FXML
  Button create;

  @FXML
  private void initialize() {
    // TODO: validate numbers only
    browse.setOnAction(event -> {
      browseDirectory();
    });
  }

  private void browseDirectory() {
    DirectoryChooser chooser = new DirectoryChooser();
    File selectedDirectory = chooser.showDialog(vBox.getScene().getWindow());
    if (selectedDirectory != null) {
      directory.setText(selectedDirectory.getPath());
    }
  }
}
