package edu.wpi.first.pathui.wizard;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageSelectorController implements Controllers {

  Image field;

  FileChooser fileChooser;

  @FXML
  private TextField fileLocation;

  @FXML
  private ImageView imageView;

  public ImageSelectorController() {
  }

  @FXML
  private void initialize() {
    field = null;
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
    fileChooser = new FileChooser();
    fileChooser.setSelectedExtensionFilter(filter);
    fileChooser.titleProperty().setValue("Select Save File");
  }

  @FXML
  void filePicker(ActionEvent event) {
    field = new Image(fileChooser.showOpenDialog(new Stage()).toURI().toString());
    fileLocation.setText(field.getUrl());
    imageView.setImage(field);
  }

  @Override
  public BooleanProperty getReadyForNext() {
    return null;
  }

  @Override
  public WizardController.Panes getNextPane() {
    return null;
  }

  @Override
  public void storeInfo() {

  }
}
