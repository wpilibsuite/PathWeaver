package edu.wpi.first.pathweaver;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class CreateProjectController {

  @FXML
  private VBox vBox;
  @FXML
  private TextField directory;
  @FXML
  private TextField timeStep;
  @FXML
  private TextField maxVelocity;
  @FXML
  private TextField maxAcceleration;
  @FXML
  private TextField maxJerk;
  @FXML
  private TextField wheelBase;

  private List<TextField> numericFields;

  @FXML
  private void initialize() {
    numericFields = List.of(timeStep, maxVelocity, maxAcceleration, maxJerk, wheelBase);
    // TODO: validate numbers only
  }

  @FXML
  private void createProject() {
    // TODO: prompt user if fields are empty
    for (TextField field : numericFields) {
      if (field.getText().trim().isEmpty()) {
        return;
      }
    }
    String folder = directory.getText().trim();
    if (folder.isEmpty()) {
      return;
    }
    ProgramPreferences.getInstance().addProject(folder);
    double t = Double.parseDouble(timeStep.getText());
    double v = Double.parseDouble(maxVelocity.getText());
    double a = Double.parseDouble(maxAcceleration.getText());
    double j = Double.parseDouble(maxJerk.getText());
    double w = Double.parseDouble(wheelBase.getText());
    ProjectPreferences.Values values = new ProjectPreferences.Values(t, v, a, j, w);
    ProjectPreferences prefs = ProjectPreferences.getInstance(folder);
    prefs.setValues(values);
    FxUtils.loadMainScreen(vBox.getScene(), getClass());
  }

  @FXML
  private void browseDirectory() {
    DirectoryChooser chooser = new DirectoryChooser();
    File selectedDirectory = chooser.showDialog(vBox.getScene().getWindow());
    if (selectedDirectory != null) {
      directory.setText(selectedDirectory.getPath());
    }
  }
}
