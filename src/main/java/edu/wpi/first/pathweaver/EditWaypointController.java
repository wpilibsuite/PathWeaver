package edu.wpi.first.pathweaver;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;

public class EditWaypointController {
  @FXML
  private TextField xPosition;
  @FXML
  private TextField yPosition;
  @FXML
  private TextField tangentX;
  @FXML
  private TextField tangentY;
  @FXML
  private CheckBox lockedTangent;
  @FXML
  private TextField pointName;

  private List<Control> controls;

  @FXML
  private void initialize() {
    controls = List.of(xPosition, yPosition, tangentX, tangentY, lockedTangent, pointName);
    controls.forEach(control -> control.setDisable(true));
    List.of(xPosition, yPosition, tangentX, tangentY)
        .forEach(textField -> textField.setTextFormatter(FxUtils.onlyDoubleText()));
  }

  /**
   * Binds the edit fields to the given wp. Allows for the unbinding and rebinding of properties as wp changes.
   * @param wp The ObservableValue for the selected waypoint.
   * @param controller The PathDisplayController to check the bounds of new waypoint values.
   */
  public void bindToWaypoint(ObservableValue<Waypoint> wp, PathDisplayController controller) {
    // When changing X and Y values, verify points are within bounds
    xPosition.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean validText = !("").equals(newValue) && !("").equals(yPosition.getText());
      boolean outsideBounds = !controller.checkBounds(Double.valueOf(newValue), Double.valueOf(yPosition.getText()));
      if (validText && outsideBounds) {
        xPosition.setText(oldValue);
      }
    });
    yPosition.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean validText = !("").equals(newValue) && !("").equals(xPosition.getText());
      boolean outsideBounds = !controller.checkBounds(Double.valueOf(xPosition.getText()), Double.valueOf(newValue));
      if (validText && outsideBounds) {
        yPosition.setText(oldValue);
      }
    });
    wp.addListener((observable, oldValue, newValue) -> {
      if (oldValue != null) {
        PathIOUtil.export(controller.getPathDirectory(), oldValue.getPath());
        unbind(oldValue);
      }
      if (newValue != null) {
        bind(newValue);
      }
    });
    enableSaving(wp, controller);
  }

  private void enableDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    field.textProperty().bindBidirectional(doubleProperty, new NumberStringConverter());
  }

  private void disableDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    field.textProperty().unbindBidirectional(doubleProperty);
    field.setText("");
  }

  private void unbind(Waypoint oldValue) {
    controls.forEach(control -> control.setDisable(true));
    disableDoubleBinding(xPosition, oldValue.xProperty());
    disableDoubleBinding(yPosition, oldValue.yProperty());
    disableDoubleBinding(tangentX, oldValue.tangentXProperty());
    disableDoubleBinding(tangentY, oldValue.tangentYProperty());
    lockedTangent.selectedProperty().unbindBidirectional(oldValue.lockTangentProperty());
    lockedTangent.setSelected(false);
    pointName.textProperty().unbindBidirectional(oldValue.nameProperty());
    pointName.setText("");
  }

  private void bind(Waypoint newValue) {
    controls.forEach(control -> control.setDisable(false));
    if (newValue.getPath().getStart() == newValue || newValue.getPath().getEnd() == newValue) {
      lockedTangent.setDisable(true);
      lockedTangent.setSelected(true);
    } else {
      lockedTangent.selectedProperty().bindBidirectional(newValue.lockTangentProperty());
    }
    enableDoubleBinding(xPosition, newValue.xProperty());
    enableDoubleBinding(yPosition, newValue.yProperty());
    enableDoubleBinding(tangentX, newValue.tangentXProperty());
    enableDoubleBinding(tangentY, newValue.tangentYProperty());
    pointName.textProperty().bindBidirectional(newValue.nameProperty());
  }

  private void enableSaving(ObservableValue<Waypoint> wp, PathDisplayController controller) {
    // Save values when out of focus
    List.of(xPosition, yPosition, tangentX, tangentY, pointName)
        .forEach(textField -> textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
          if (!newValue && wp.getValue() != null) {
            PathIOUtil.export(controller.getPathDirectory(), wp.getValue().getPath());
          }
        }));

    lockedTangent.selectedProperty()
        .addListener(__ -> {
          if (wp.getValue() != null) {
            PathIOUtil.export(controller.getPathDirectory(), wp.getValue().getPath());
          }
        });
  }
}
