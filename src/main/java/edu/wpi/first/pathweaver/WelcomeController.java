package edu.wpi.first.pathweaver;

import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WelcomeController {

  @FXML
  BorderPane borderPane;
  @FXML
  ListView<String> projects;
  @FXML
  Button createProject;
  @FXML
  Button importProject;
  @FXML
  Button help;

  @FXML
  private void initialize() {
    ProgramPreferences prefs = ProgramPreferences.getInstance();
    ObservableList<String> items = FXCollections.observableArrayList(prefs.getRecentProjects());
    projects.setItems(items);

    projects.setOnMouseClicked(event -> {
      String folder = projects.getSelectionModel().getSelectedItem();
      loadProject(folder);
    });

    createProject.setOnAction(event -> {
      createProject();
    });

    importProject.setOnAction(event -> {
      importProject();
    });
  }

  private void createProject() {
  }

  private void loadProject(String folder) {
    try {
      Pane root = FXMLLoader.load(getClass().getResource("main.fxml"));
      Scene scene = borderPane.getScene();
      Stage primaryStage = (Stage) scene.getWindow();
      primaryStage.resizableProperty().setValue(true);
      scene.getStylesheets().add("/edu/wpi/first/pathweaver/style.css");
      scene.setRoot(root);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void importProject() {
    DirectoryChooser chooser = new DirectoryChooser();
    File selectedDirectory = chooser.showDialog(borderPane.getScene().getWindow());
    if (selectedDirectory != null) {
      ProgramPreferences.getInstance().addProject(selectedDirectory.getPath());
      loadProject(selectedDirectory.getPath());
    }
  }
}
