package edu.wpi.first.pathui;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

public class PathDisplayController {
  @FXML private ImageView backgroundImage;
  @FXML private Pane drawPane;
  @FXML private Group group;
  @FXML private Pane topPane;

  private Image image;
  private Scale scale;
  @FXML
  private void initialize() {

    image = new Image("edu/wpi/first/pathui/2018-field.jpg");
    backgroundImage.setImage(image);

    scale = new Scale();
    scale.xProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth()/image.getWidth(),topPane.getHeight()/image.getHeight()),
        topPane.widthProperty(),topPane.heightProperty()));
    scale.yProperty().bind(Bindings.createDoubleBinding(() ->
            Math.min(topPane.getWidth()/image.getWidth(),topPane.getHeight()/image.getHeight()),
        topPane.widthProperty(),topPane.heightProperty()));

    group.getTransforms().add(scale);


    setupDrawPaneSizing();


    DisplayPath firstPath = new DisplayPath();
    drawPane.getChildren().add(firstPath.getPathPane());
    firstPath.getPathPane().minHeightProperty().bind(drawPane.heightProperty());
    firstPath.getPathPane().minWidthProperty().bind(drawPane.widthProperty());
    firstPath.getPathPane().maxHeightProperty().bind(drawPane.heightProperty());
    firstPath.getPathPane().maxWidthProperty().bind(drawPane.widthProperty());

    DisplayPath secondPath = new DisplayPath();
    drawPane.getChildren().add(secondPath.getPathPane());
    secondPath.getPathPane().minHeightProperty().bind(drawPane.heightProperty());
    secondPath.getPathPane().minWidthProperty().bind(drawPane.widthProperty());
    secondPath.getPathPane().maxHeightProperty().bind(drawPane.heightProperty());
    secondPath.getPathPane().maxWidthProperty().bind(drawPane.widthProperty());

  }
void setupDrawPaneSizing(){

  drawPane.setMaxWidth(image.getWidth());
  drawPane.setMinWidth(image.getWidth());
  drawPane.setMaxHeight(image.getHeight());
  drawPane.setMinHeight(image.getHeight());
  drawPane.setPrefHeight(image.getHeight());
  drawPane.setPrefWidth(image.getWidth());

}


}
