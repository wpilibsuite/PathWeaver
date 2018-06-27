package edu.wpi.first.pathui.wizard;

import java.io.IOException;
import java.util.Stack;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Wizard{

  private Stage stage;
  private WizardController controller;

  public Wizard() throws IOException {
    stage= new Stage();
    stage.setTitle("New PathUI Project");
    FXMLLoader fxmlLoader = new FXMLLoader(WizardController.class.getResource("WizardController.fxml"));
    stage.setScene(new Scene(fxmlLoader.load()));
    controller = fxmlLoader.getController();
  }

  public void show(){
    stage.show();
  }//make project

  public void hide(){
    stage.hide();
  }


}

