package edu.wpi.first.pathui.wizard;

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

  public StartSceneController() {
  }

  @FXML
  private void initialize() {
    setupButtons();
  }


  private void setupButtons() {
    loadProject.setToggleGroup(options);
    newProject.setToggleGroup(options);
    newProject.setSelected(true);
    newProject.fire();
  }

  public WizardController.Panes getNextPane() {
    return nextPane;
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
