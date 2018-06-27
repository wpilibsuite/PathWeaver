package edu.wpi.first.pathui.wizard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoadProjectController implements Controllers {

  @FXML
  private TextField fileLocation;

  @FXML
  private Button filePicker;

  @FXML
  void filePicker(ActionEvent event) {
  }

  public LoadProjectController(){}

  @Override
  public WizardController.Panes getNextPane() {
    return WizardController.Panes.StartScene;
  }
}
