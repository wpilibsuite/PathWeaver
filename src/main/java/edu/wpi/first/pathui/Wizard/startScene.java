package edu.wpi.first.pathui.Wizard;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class startScene implements SceneSwitch{
  private Scene scene;
  private StackPane pane;
  private RadioButton rb1;
  private RadioButton rb2;
  ToggleGroup buttonGroup;
  VBox vBox;

  public startScene(){
    pane=new StackPane();
    buttonGroup=new ToggleGroup();
    rb1=new RadioButton("Load Project");
    rb2=new RadioButton("New Project");
    vBox=new VBox(20,rb1,rb2);
    setup();
  }

  private void setup() {
    pane.setPrefSize(300,300);
    rb1.setToggleGroup(buttonGroup);
    rb1.setSelected(true);
    rb2.setToggleGroup(buttonGroup);
    vBox.setAlignment(Pos.CENTER);
    pane.getChildren().add(vBox);
    scene=new Scene(pane);
  }

  public Scene getScene(){
    return this.scene;
  }

}
