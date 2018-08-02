package edu.wpi.first.pathweaver;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PathWeaver extends Application {
  @Override
  public void start(Stage primaryStage) throws IOException {
    Pane root = FXMLLoader.load(getClass().getResource("welcomeScreen.fxml"));
    Scene scene = new Scene(root);
    primaryStage.resizableProperty().setValue(false);
    primaryStage.setScene(scene);
    primaryStage.show();

    primaryStage.setOnCloseRequest(value -> {
      ProgramPreferences.getInstance().saveSizeAndPosition(primaryStage);
    });

    Loggers.setupLoggers();
  }
}
