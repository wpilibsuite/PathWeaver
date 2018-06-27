package edu.wpi.first.pathui.wizard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.FieldEditor;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.FieldLoader;
import static edu.wpi.first.pathui.wizard.WizardController.Panes.ImageSelector;


public class FieldCreatorController implements Controllers{

  @FXML
  private RadioButton loadField;

  @FXML
  private RadioButton editField;

  @FXML
  private RadioButton newField;

  private static WizardController.Panes nextPane;

  private ToggleGroup options = new ToggleGroup();

  public FieldCreatorController(){
  }

  @FXML
  private void initialize() {
    nextPane=ImageSelector;
    setupButtons();
  }


  private void setupButtons() {
    loadField.setToggleGroup(options);
    newField.setToggleGroup(options);
    editField.setToggleGroup(options);
    newField.setSelected(true);
    newField.fire();
  }

  public WizardController.Panes getNextPane() {
    return nextPane;
  }

  @Override
  public void storeInfo() {
    //does Nothing
  }
  @FXML
  void editField(ActionEvent event) {
    nextPane=FieldEditor;
  }

  @FXML
  void loadField(ActionEvent event) {
    nextPane=FieldLoader;
  }

  @FXML
  void newField(ActionEvent event) {
    nextPane=ImageSelector;
  }

}
