package edu.wpi.first.pathweaver;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class WelcomeController {
  public static boolean dark;

  @FXML private BorderPane borderPane;
  @FXML private ListView<String> projects;
  @FXML private Label version;

  @FXML
  private void initialize() {
    version.setText(PathWeaver.getVersion());

    projects.getItems().setAll(
        ProgramPreferences.getInstance().getRecentProjects());

    projects.setOnMouseClicked(event -> {
      String folder = projects.getSelectionModel().getSelectedItem();
      if (folder != null) {
        loadProject(folder);
      }
    });
  }

  @FXML
  private void createProject() {
    try {
      Pane root = FXMLLoader.load(getClass().getResource("createProject.fxml"));
      Scene scene = borderPane.getScene();
      scene.setRoot(root);
    } catch (IOException e) {
      Logger log = Logger.getLogger(getClass().getName());
      log.log(Level.WARNING, "Couldn't load create project screen", e);
    }
  }

  private void loadProject(String folder) {
    if (ProjectPreferences.projectExists(folder)) {
      ProgramPreferences.getInstance().addProject(folder);
      ProjectPreferences.getInstance(folder);
      FxUtils.loadMainScreen(borderPane.getScene(), getClass());
    } else {
      invalidProject(folder);
    }
  }

  private void invalidProject(String folder) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    FxUtils.applyDarkMode(alert);
    alert.setTitle("Project Does Not Exist!");
    alert.setHeaderText("The project does not exist.");
    alert.setContentText("What do you want to do?");
    ButtonType recreate = new ButtonType("Create it");
    ButtonType remove = new ButtonType("Remove it");
    ButtonType nothing = new ButtonType("Nothing");
    alert.getButtonTypes().setAll(recreate, remove, nothing);
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == recreate) {
      ProjectPreferences.getInstance(folder);
      createProject();
    } else if (result.get() == remove) {
      ProgramPreferences.getInstance().removeProject(folder);
      projects.getItems().remove(folder);
    }
  }

  @FXML
  private void importProject() {
    DirectoryChooser chooser = new DirectoryChooser();
    File selectedDirectory =
        chooser.showDialog(borderPane.getScene().getWindow());
    if (selectedDirectory != null) {
      ProgramPreferences.getInstance().addProject(selectedDirectory.getPath());
      loadProject(selectedDirectory.getPath());
    }
  }

  @FXML
  private void help() throws URISyntaxException, IOException {
    Desktop.getDesktop().browse(new URI(
        "https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/pathweaver/index.html"));
  }

  @FXML
  private void darkToggle() {
    final String darkModeFile = "dark.css";
    final boolean darkIsOn = PathWeaver.mainScene.getStylesheets().contains(
        getClass().getResource(darkModeFile).toExternalForm());
    if (darkIsOn) {
      PathWeaver.mainScene.getStylesheets().removeAll(
          getClass().getResource(darkModeFile).toExternalForm());
    } else {
      PathWeaver.mainScene.getStylesheets().add(
          getClass().getResource(darkModeFile).toExternalForm());
    }
    dark = !darkIsOn;
  }

  @FXML
  private void robotOutlineToggle() {

  }

  @FXML
  private void wheelPathToggle() {

  }
}
