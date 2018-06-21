package edu.wpi.first.pathui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PathUI extends Application {
  @Override
  public void start(Stage primaryStage) throws IOException {
    Pane root = FXMLLoader.load(getClass().getResource("main.fxml"));
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/edu/wpi/first/pathui/style.css");
    primaryStage.setScene(scene);
    primaryStage.show();

    Loggers.setupLoggers();

  }
}
