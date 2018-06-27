package edu.wpi.first.pathui.wizard;

import java.util.Stack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static edu.wpi.first.pathui.wizard.WizardController.Panes.StartScene;

public class WizardController {

  @FXML
  private StackPane topPane;

  @FXML
  private Button previous;

  @FXML
  private Button next;

  @FXML
  private Button cancel;

  @FXML
  private Button help;

  @FXML
  private Pane startScreen;

  @FXML
  private Pane loadProject;


  public enum Panes {
    StartScene,
    LoadProject,
    NewProjectSave
  }



  private Panes currentPane = StartScene;

  private Stack<Panes> previousPanes = new Stack<Panes>();

  private Controllers currentController;

  @FXML
  private void initialize() {
    setupPanes();
    goToPane(currentPane);
    currentController = new StartSceneController();
  }

  @FXML
  void close(ActionEvent event) {

  }

  @FXML
  void help(ActionEvent event) {

  }

  @FXML
  void nextScene(ActionEvent event) {
    System.out.println("storing old pane");
    previousPanes.add(currentPane);
    System.out.println("hiding old Panes");
    setupPanes();
    System.out.println("reciving new pane");
    currentPane = currentController.getNextPane();
    System.out.println("going to new Plane");
    goToPane(currentPane);

  }

  @FXML
  void previousScene(ActionEvent event) {
  }

  void goToPane(Panes pane) {
    System.out.println("going to " +pane);
    switch (pane) {
      case StartScene:
        topPane.getChildren().setAll(startScreen);
        startScreen.visibleProperty().setValue(true);
        currentController = new StartSceneController();
        break;

      case LoadProject:
        topPane.getChildren().setAll(loadProject);
        loadProject.visibleProperty().setValue(true);
        currentController=new LoadProjectController();
        break;

      case NewProjectSave:
        break;

    }

  }

  void setupPanes(){
    startScreen.visibleProperty().setValue(false);
    loadProject.visibleProperty().setValue(false);
  }
}


