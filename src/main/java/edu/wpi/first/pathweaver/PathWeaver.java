package edu.wpi.first.pathweaver;

import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.pathweaver.extensions.ExtensionManager;

import edu.wpi.first.wpiutil.CombinedRuntimeLoader;
import edu.wpi.first.wpiutil.WPIUtilJNI;
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PathWeaver extends Application {
  static Scene mainScene;

  @Override
  public void start(Stage primaryStage) throws IOException {
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    WPIMathJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(PathWeaver.class,  "wpiutiljni", "wpimathjni");

    ExtensionManager.getInstance().refresh();
    Pane root = FXMLLoader.load(getClass().getResource("welcomeScreen.fxml"));
    this.mainScene = new Scene(root);
    primaryStage.setTitle("PathWeaver - " + getVersion());
    // Work around dialog bug
    // See https://stackoverflow.com/questions/55190380/javafx-creates-alert-dialog-which-is-too-small
    primaryStage.setResizable(true);
    primaryStage.onShownProperty().addListener(e -> Platform.runLater(() -> primaryStage.setResizable(false)));
    primaryStage.setScene(this.mainScene);
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
