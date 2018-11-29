package edu.wpi.first.pathweaver;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

import javax.measure.Unit;
import javax.measure.quantity.Length;

@SuppressWarnings("PMD.TooManyFields")
public class CreateProjectController {

  @FXML
  private Label title;
  @FXML
  private Button browse;
  @FXML
  private Button create;
  @FXML
  private Button cancel;
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
  private ChoiceBox<Game> game;
  @FXML
  private ChoiceBox<Unit<Length>> length;
  @FXML
  private Label velocityLabel;
  @FXML
  private Label accelerationLabel;
  @FXML
  private Label jerkLabel;
  @FXML
  private Label wheelBaseLabel;

  private boolean editing = false;

  @FXML
  @SuppressWarnings("PMD.NcssCount")
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

    game.getItems().addAll(Game.getGames());
    game.converterProperty().setValue(new StringConverter<>() {
      @Override
      public String toString(Game object) {
        return object.getName();
      }

      @Override
      public Game fromString(String string) {
        return Game.fromPrettyName(string);
      }
    });

    length.getItems().addAll(PathUnits.LENGTHS);
    length.getSelectionModel().selectFirst();
    length.setConverter(new StringConverter<>() {
      @Override
      public String toString(Unit<Length> object) {
        return object.getName();
      }

      @Override
      public Unit<Length> fromString(String string) {
        throw new UnsupportedOperationException();
      }
    });

    var lengthUnit = EasyBind.monadic(length.getSelectionModel().selectedItemProperty());
    List.of(velocityLabel, maxVelocity).forEach(control -> control.tooltipProperty()
        .setValue(new Tooltip("The maximum velocity your robot can travel.")));
    velocityLabel.textProperty().bind(lengthUnit.map(PathUnits::velocity));
    List.of(accelerationLabel, maxAcceleration).forEach(control -> control.tooltipProperty()
        .setValue(new Tooltip("The maximum capable acceleration of your robot.")));
    accelerationLabel.textProperty().bind(lengthUnit.map(PathUnits::acceleration));
    List.of(jerkLabel, maxJerk).forEach(control -> control.tooltipProperty()
        .setValue(new Tooltip("The maximum jerk to use when calculating motion profiles."
            + " This is user preference.")));
    jerkLabel.textProperty().bind(lengthUnit.map(PathUnits::jerk));
    List.of(wheelBaseLabel, wheelBase).forEach(control -> control.tooltipProperty()
        .setValue(new Tooltip("Distance between the left and right of the wheel base.")));
    wheelBaseLabel.textProperty().bind(lengthUnit.map(Unit::getSymbol));

    List.of(velocityLabel, maxVelocity, accelerationLabel, maxAcceleration, jerkLabel, maxJerk, wheelBaseLabel,
        wheelBase).forEach(label -> label.tooltipProperty().get().setShowDelay(new Duration(300)));

    // We are editing a project
    if (ProjectPreferences.getInstance() != null) {
      setupEditProject();
    }
  }

  @FXML
  private void createProject() {
    String folderString = directory.getText().trim();
    // create a "PathWeaver" subdirectory if not editing an existing project
    File directory = editing ? new File(folderString) : new File(folderString, "PathWeaver");
    editing = false;
    directory.mkdir();
    ProgramPreferences.getInstance().addProject(directory.getAbsolutePath());
    String lengthUnit = length.getValue().getName();
    double timeDelta = Double.parseDouble(timeStep.getText());
    double velocityMax = Double.parseDouble(maxVelocity.getText());
    double accelerationMax = Double.parseDouble(maxAcceleration.getText());
    double jerkMax = Double.parseDouble(maxJerk.getText());
    double wheelBaseDistance = Double.parseDouble(wheelBase.getText());
    ProjectPreferences.Values values = new ProjectPreferences.Values(lengthUnit, timeDelta, velocityMax,
        accelerationMax, jerkMax, wheelBaseDistance, game.getValue().getName());
    ProjectPreferences prefs = ProjectPreferences.getInstance(directory.getAbsolutePath());
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

  private void setupEditProject() {
    ProjectPreferences.Values values = ProjectPreferences.getInstance().getValues();
    directory.setText(ProjectPreferences.getInstance().getDirectory());
    create.setText("Edit Project");
    title.setText("Edit Project");
    browse.setVisible(false);
    cancel.setVisible(false);
    game.setValue(Game.fromPrettyName(values.getGameName()));
    length.setValue(values.getLengthUnit());
    timeStep.setText(String.valueOf(values.getTimeStep()));
    maxVelocity.setText(String.valueOf(values.getMaxVelocity()));
    maxAcceleration.setText(String.valueOf(values.getMaxAcceleration()));
    maxJerk.setText(String.valueOf(values.getMaxJerk()));
    wheelBase.setText(String.valueOf(values.getWheelBase()));
    editing = true;
  }
}
