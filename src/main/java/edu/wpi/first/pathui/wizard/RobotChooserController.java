package edu.wpi.first.pathui.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.FieldEditor;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.FieldLoader;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.ImageSelector;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.RobotCreator;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.RobotEditor;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.RobotLoader;

public class RobotChooserController implements Controllers {
  @FXML
  private RadioButton loadRobot;

  @FXML
  private RadioButton editRobot;

  @FXML
  private RadioButton newRobot;

  private static WizardController.Panes nextPane;

  private ToggleGroup options = new ToggleGroup();

  private BooleanProperty readyForNext=new SimpleBooleanProperty(false);

  public RobotChooserController(){
  }

  @FXML
  private void initialize() {
    nextPane=RobotCreator;
    setupButtons();
  }


  private void setupButtons() {
    loadRobot.setToggleGroup(options);
    newRobot.setToggleGroup(options);
    editRobot.setToggleGroup(options);
    newRobot.setSelected(true);
    newRobot.fire();
  }

  @Override
  public BooleanProperty getReadyForNext() {
    return readyForNext;
  }

  public WizardController.Panes getNextPane() {
    return nextPane;
  }

  @Override
  public void storeInfo() {
    //does Nothing
  }
  @FXML
  void editRobot(ActionEvent event) {
    nextPane=RobotEditor;
  }

  @FXML
  void loadRobot(ActionEvent event) {
    nextPane=RobotLoader;
  }

  @FXML
  void newRobot(ActionEvent event) {
    nextPane=RobotCreator;
  }

}

