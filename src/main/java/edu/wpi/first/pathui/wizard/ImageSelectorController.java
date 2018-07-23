package edu.wpi.first.pathui.wizard;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.RobotChooser;

public class ImageSelectorController implements Controllers {

  Image image;

  FileChooser fileChooser;

  @FXML
  private TextField fileLocation;

  @FXML
  private ImageView imageView;

  @FXML
  private Pane drawPane;

  public ImageSelectorController() {
  }

  @FXML
  private void initialize() {
    image = null;
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
    fileChooser = new FileChooser();
    fileChooser.setSelectedExtensionFilter(filter);
    fileChooser.titleProperty().setValue("Select Save File");
  }

  @FXML
  void filePicker(ActionEvent event) {
    image = new Image(fileChooser.showOpenDialog(new Stage()).toURI().toString());
    fileLocation.setText(image.getUrl());
    imageView.setImage(image);
    generateAreaSelector();
  }

  void generateAreaSelector(){

  }

  @Override
  public BooleanProperty getReadyForNext() {
    return null;
  }

  @Override
  public WizardController.Panes getNextPane() {
    return RobotChooser;
  }

  @Override
  public void storeInfo() {

  }
}
