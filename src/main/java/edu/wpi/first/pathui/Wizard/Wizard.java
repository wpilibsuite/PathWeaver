package edu.wpi.first.pathui.Wizard;

import java.util.ArrayList;
import java.util.Stack;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Wizard{

  private Stage stage;
  private startScene projectLoader;
  private Stack<SceneSwitch> sceneSwitcher;
  private SceneSwitch currentScene;


  public Wizard(){
    stage= new Stage();
    projectLoader=new startScene();
    currentScene=projectLoader;
    setup(stage);
  }

  private void setup(Stage stage) {
    stage.setTitle("New PathUI Project");
    stage.setScene(currentScene.getScene());
  }

  public void show(){
    stage.show();
  }

  public void hide(){
    stage.hide();
  }

  private void nextScene(SceneSwitch scene){
    sceneSwitcher.add(currentScene);
    currentScene=scene;
    stage.setScene(currentScene.getScene());
  }

  private void previousScene(){
    currentScene=sceneSwitcher.pop();
    stage.setScene(currentScene.getScene());
  }




}

