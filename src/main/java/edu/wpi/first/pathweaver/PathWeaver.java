package edu.wpi.first.pathweaver;

import edu.wpi.first.math.jni.TrajectoryUtilJNI;
import edu.wpi.first.pathweaver.extensions.ExtensionManager;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PathWeaver extends Application {
  public static Scene mainScene;

  @Override
  public void start(Stage primaryStage) throws IOException {
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    TrajectoryUtilJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(PathWeaver.class, "wpiutiljni",
        "wpimathjni");

    ExtensionManager.getInstance().refresh();
    Pane root = FXMLLoader.load(getClass().getResource("welcomeScreen.fxml"));
    this.mainScene = new Scene(root);
    primaryStage.setTitle("PathWeaver - " + getVersion());
    // Work around dialog bug
    // See
    // https://stackoverflow.com/questions/55190380/javafx-creates-alert-dialog-which-is-too-small
    primaryStage.setResizable(true);
    primaryStage.onShownProperty().addListener(
        e -> Platform.runLater(() -> primaryStage.setResizable(false)));
    primaryStage.setScene(this.mainScene);
    primaryStage.show();
    Loggers.setupLoggers();
    var warn = new Alert(AlertType.WARNING);
    warn.setTitle("PathWeaver is deprecated!");
    warn.setHeaderText("PathWeaver is deprecated and will be removed by 2027.");
    warn.setContentText("Consider using Choreo or PathPlanner instead.");
    ButtonType learnMore = new ButtonType("Learn more");
    ButtonType okay = new ButtonType("Ok");
    warn.getButtonTypes().remove(0);
    warn.getButtonTypes().addAll(learnMore, okay);
    var result = warn.showAndWait();
    if (result.get().getText().equals("Learn more")) {
      try {
        Desktop.getDesktop().browse(new URI(
            "https://docs.wpilib.org/en/stable/docs/software/pathplanning/pathweaver/introduction.html#deprecation-notice"));
      } catch (IOException | URISyntaxException e) {
      }
    }
  }

  /**
   * The version of this build of PathWeaver.
   * 
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
