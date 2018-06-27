package edu.wpi.first.pathui.wizard;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LoadProjectController implements Controllers {

  @FXML
  private TextField fileLocation;

  @FXML
  private Button filePicker;

  File saveFile;

  FileChooser fileChooser;
  
  
  @FXML
  private void initialize(){
    saveFile=null;
    fileChooser=new FileChooser();
    fileChooser.titleProperty().setValue("Select Save File");
    }
  @FXML
  void filePicker(ActionEvent event) {
    saveFile = fileChooser.showOpenDialog(new Stage());
    fileLocation.setText(saveFile.getPath());
  }

  public LoadProjectController(){}

  @Override
  public WizardController.Panes getNextPane() {
    return WizardController.Panes.StartScene;
  }
}
