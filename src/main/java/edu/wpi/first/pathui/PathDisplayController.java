package edu.wpi.first.pathui;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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


    /*DisplayPath firstPath = new DisplayPath(drawPane);
    drawPane.getChildren().add(firstPath.getGroup());
    firstPath.getGroup().minHeightProperty().bind(drawPane.heightProperty());
    firstPath.getGroup().minWidthProperty().bind(drawPane.widthProperty());
    firstPath.getGroup().maxHeightProperty().bind(drawPane.heightProperty());
    firstPath.getGroup().maxWidthProperty().bind(drawPane.widthProperty());*/

    DisplayPath secondPath = new DisplayPath();
    drawPane.getChildren().add(secondPath.getGroup());
    secondPath.getGroup().minHeightProperty().bind(drawPane.heightProperty());
    secondPath.getGroup().minWidthProperty().bind(drawPane.widthProperty());
    secondPath.getGroup().maxHeightProperty().bind(drawPane.heightProperty());
    secondPath.getGroup().maxWidthProperty().bind(drawPane.widthProperty());

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
