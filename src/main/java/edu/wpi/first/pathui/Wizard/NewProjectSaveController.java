package edu.wpi.first.pathui.wizard;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.FieldCreator;

public class NewProjectSaveController implements Controllers {

  @FXML
  private TextField projectName;

  @FXML
  private TextField projectLocation;

  private DirectoryChooser directoryChooser;

  private File directory;

  private String pName;

  private BooleanProperty readyForNext=new SimpleBooleanProperty(false);



  @FXML
  private void initialize() {
    pName = "Unnamed Project";
    directory = null;
    directoryChooser = new DirectoryChooser();
    projectName.setText(pName);

  }

  public NewProjectSaveController() {
  }


  @FXML
  void directoryPicker(ActionEvent event) {
    directory = directoryChooser.showDialog(new Stage());
    projectLocation.setText(directory.getPath());
  }


  @Override
  public BooleanProperty getReadyForNext() {
    return readyForNext;
  }

  @Override
  public WizardController.Panes getNextPane() {
    return FieldCreator;
  }

  @Override
  public void storeInfo() {

  }
}
