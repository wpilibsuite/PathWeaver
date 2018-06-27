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

  @FXML
  private Pane newProjectSave;


  public enum Panes {
    StartScene,
    LoadProject,
    NewProjectSave,
    FieldCreator
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
    previousPanes.add(currentPane);
    setupPanes();
    currentController.storeInfo();
    currentPane = currentController.getNextPane();
    goToPane(currentPane);

  }

  @FXML
  void previousScene(ActionEvent event) {
    currentPane=previousPanes.pop();
    setupPanes();
    goToPane(currentPane);
  }

  void goToPane(Panes pane) {
    System.out.println("going to " +pane);
    switch (pane) {
      case StartScene:
        topPane.getChildren().setAll(startScreen);
        startScreen.visibleProperty().setValue(true);
        currentController = new StartSceneController();
        previous.disableProperty().setValue(true);
        break;

      case LoadProject:
        topPane.getChildren().setAll(loadProject);
        loadProject.visibleProperty().setValue(true);
        currentController=new LoadProjectController();
        previous.disableProperty().setValue(false);
        break;

      case NewProjectSave:
        topPane.getChildren().setAll(newProjectSave);
        newProjectSave.visibleProperty().setValue(true);
        currentController = new NewProjectSaveController();
        previous.disableProperty().setValue(false);
        break;

      case FieldCreator:
        break;

    }

  }

  void setupPanes(){
    startScreen.visibleProperty().setValue(false);
    loadProject.visibleProperty().setValue(false);
    newProjectSave.visibleProperty().setValue(false);
  }
}


