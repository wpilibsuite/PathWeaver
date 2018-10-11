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
    primaryStage.setTitle("PathWeaver - " + getVersion());
    primaryStage.resizableProperty().setValue(false);
    primaryStage.setScene(scene);
    primaryStage.show();
    Loggers.setupLoggers();
  }

  /**
   * The version of this build of PathWeaver.
   * @return String representing the version of PathWeaver.
   */
  public static String getVersion() {
    String version = PathWeaver.class.getPackage().getImplementationVersion();
    if (version == null) {
      return "Development";
    }
    return version;
  }
}
