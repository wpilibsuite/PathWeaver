package edu.wpi.first.pathui.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.LoadProject;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.NewProjectSave;

public class StartSceneController implements Controllers {

  @FXML
  private AnchorPane mainPane;

  @FXML
  private RadioButton loadProject;

  @FXML
  private RadioButton newProject;

  private static WizardController.Panes nextPane;

  private ToggleGroup options = new ToggleGroup();

  private BooleanProperty readyForNext=new SimpleBooleanProperty(false);

  public StartSceneController(){
  }

  @FXML
  private void initialize() {
    nextPane=NewProjectSave;
    setupButtons();
    readyForNext.setValue(true);
  }


  private void setupButtons() {
    loadProject.setToggleGroup(options);
    newProject.setToggleGroup(options);
    newProject.setSelected(true);
    newProject.fire();
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
  void loadProject(ActionEvent event) {
    nextPane = LoadProject;
  }

  @FXML
  void newProject(ActionEvent event) {
    nextPane = NewProjectSave;
  }



}
