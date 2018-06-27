package edu.wpi.first.pathui.wizard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

public class StartSceneController {

  @FXML
  private AnchorPane mainPane;

  @FXML
  private RadioButton loadProject;

  @FXML
  private RadioButton newProject;

  private ToggleGroup options;

  public StartSceneController(){
    setupButtons();
  }

  private void setupButtons() {
    loadProject.setToggleGroup(options);
    newProject.setToggleGroup(options);
  }

  public AnchorPane getMainPane(){
    return this.mainPane;
  }

  public


  @FXML
  void loadProject(ActionEvent event) {

  }

  @FXML
  void newProject(ActionEvent event) {

  }



}
