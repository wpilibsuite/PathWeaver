package edu.wpi.first.pathweaver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

public class WelcomeController {

  @FXML
  private BorderPane borderPane;
  @FXML
  private ListView<String> projects;
  @FXML
  private Button createProject;
  @FXML
  private Button importProject;
  @FXML
  private Button help;

  @FXML
  private void initialize() {
    ProgramPreferences prefs = ProgramPreferences.getInstance();
    ObservableList<String> items = FXCollections.observableArrayList(prefs.getRecentProjects());
    projects.setItems(items);

    projects.setOnMouseClicked(event -> {
      String folder = projects.getSelectionModel().getSelectedItem();
      if (folder != null) {
        loadProject(folder);
      }
    });

    createProject.setOnAction(event -> {
      createProject();
    });

    importProject.setOnAction(event -> {
      importProject();
    });
  }

  private void createProject() {
    try {
      Pane root = FXMLLoader.load(getClass().getResource("createProject.fxml"));
      Scene scene = borderPane.getScene();
      scene.setRoot(root);
    } catch (IOException e) {
      Logger log = LogManager.getLogManager().getLogger(getClass().getName());
      log.log(Level.WARNING, e.getMessage());
    }

  }

  private void loadProject(String folder) {
    ProgramPreferences.getInstance().addProject(folder);
    ProjectPreferences.getInstance(folder);
    FxUtils.loadMainScreen(borderPane.getScene(), getClass());
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
