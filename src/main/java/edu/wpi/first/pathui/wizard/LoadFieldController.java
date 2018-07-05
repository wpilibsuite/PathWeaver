package edu.wpi.first.pathui.wizard;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LoadFieldController implements Controllers{
  @FXML
  private TextField fileLocation;


  File saveFile;

  FileChooser fileChooser;

  private BooleanProperty readyForNext=new SimpleBooleanProperty(false);

  @FXML
  private void initialize() {
    saveFile = null;
    fileChooser = new FileChooser();
    fileChooser.titleProperty().setValue("Select Save File");
  }

  @FXML
  void filePicker(ActionEvent event) {
    saveFile = fileChooser.showOpenDialog(new Stage());
    fileLocation.setText(saveFile.getPath());
  }

  public LoadFieldController() {
  }

  @Override
  public BooleanProperty getReadyForNext() {
    return readyForNext;
  }

  @Override
  public WizardController.Panes getNextPane() {
    return WizardController.Panes.StartScene;
  }

  @Override
  public void storeInfo() {
    //need to save location of files
  }
}
