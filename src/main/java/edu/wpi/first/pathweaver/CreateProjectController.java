package edu.wpi.first.pathweaver;

import java.io.File;
import java.io.IOException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

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
  private TextField robotWidth;
  @FXML
  private TextField robotLength;
  @FXML
  private ChoiceBox<Game> game;

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
    bind = bind.or(game.valueProperty().isNull());
    create.disableProperty().bind(bind);


    // Validate that numericFields contain decimal numbers
    numericFields.forEach(textField -> textField.setTextFormatter(FxUtils.onlyPositiveDoubleText()));

    if (ProjectPreferences.getInstance() != null) {
      directory.setText(ProjectPreferences.getInstance().getDirectory());
    }

    game.getItems().addAll(Game.values());
    game.converterProperty().setValue(new StringConverter<>() {
      @Override
      public String toString(Game object) {
        return object.toPrettyName();
      }

      @Override
      public Game fromString(String string) {
        return Game.fromPrettyName(string);
      }
    });
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
    double robotWidthValue = Double.parseDouble(robotWidth.getText());
    double robotLengthValue = Double.parseDouble(robotLength.getText());
    ProjectPreferences.Values values = new ProjectPreferences.Values(timeDelta, velocityMax, accelerationMax,
        jerkMax, wheelBaseDistance, robotWidthValue, robotLengthValue, game.getValue());
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

  @FXML
  private void cancel() throws IOException {
    Pane root = FXMLLoader.load(getClass().getResource("welcomeScreen.fxml"));
    vBox.getScene().setRoot(root);
  }
}
