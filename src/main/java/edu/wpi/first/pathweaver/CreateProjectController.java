package edu.wpi.first.pathweaver;

import java.io.File;
import java.util.function.UnaryOperator;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

public class CreateProjectController {

  @FXML
  private Button create;
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

  @FXML
  private void initialize() {
    ObservableList<TextField> numericFields = FXCollections.observableArrayList(timeStep, maxVelocity,
        maxAcceleration, maxJerk, wheelBase);
    ObservableList<TextField> allFields = FXCollections.observableArrayList(numericFields);
    allFields.add(directory);

    BooleanBinding bind = new SimpleBooleanProperty(true).not();
    for (TextField field : allFields) {
      bind = bind.or(field.textProperty().isEmpty());
    }
    create.disableProperty().bind(bind);



    UnaryOperator<TextFormatter.Change> onlyDoubleText = c -> {
      String text = c.getControlNewText();
      if (text.matches("^\\d*\\.?\\d*$")) {
        return c;
      } else {
        return null;
      }
    };
    // Validate that numericFields contain decimal numbers
    numericFields.forEach(textField -> textField.setTextFormatter(new TextFormatter<Object>(onlyDoubleText)));
  }

  @FXML
  private void createProject() {
    String folder = directory.getText().trim();
    ProgramPreferences.getInstance().addProject(folder);
    double timeDelta = Double.parseDouble(timeStep.getText());
    double velocityMax = Double.parseDouble(maxVelocity.getText());
    double accelerationMax = Double.parseDouble(maxAcceleration.getText());
    double jerkMax = Double.parseDouble(maxJerk.getText());
    double wheelBaseDistance = Double.parseDouble(wheelBase.getText());
    ProjectPreferences.Values values = new ProjectPreferences.Values(timeDelta, velocityMax, accelerationMax,
        jerkMax, wheelBaseDistance);
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
