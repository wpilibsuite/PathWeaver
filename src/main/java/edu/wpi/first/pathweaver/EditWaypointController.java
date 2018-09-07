package edu.wpi.first.pathweaver;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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

  private ChangeListener<String> nameListener;

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
      if (validText && !controller.checkBounds(Double.valueOf(newValue), Double.valueOf(yPosition.getText()))) {
        xPosition.setText(oldValue);
      }
    });
    yPosition.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean validText = !("").equals(newValue) && !("").equals(xPosition.getText());
      if (validText && !controller.checkBounds(Double.valueOf(xPosition.getText()), Double.valueOf(newValue))) {
        yPosition.setText(oldValue);
      }
    });
    wp.addListener((observable, oldValue, newValue) -> {
      if (oldValue != null) {
        SaveManager.getInstance().addChange(oldValue.getPath());
        unbind(oldValue);
      }
      if (newValue != null) {
        bind(newValue);
      }
    });
    enableSaving(wp);
    lockTangentOnEdit();
  }

  private void enableDoubleBinding(TextField field, DoubleProperty doubleProperty) {
    NumberStringConverter converter = new NumberStringConverter() {
      @Override
      public Number fromString(String value) {
        // Don't parse the beginning of a negative number
        if ("-".equals(value)) {
          return null;
        } else {
          return super.fromString(value);
        }
      }
    };
    field.textProperty().bindBidirectional(doubleProperty, converter);
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
    pointName.textProperty().removeListener(nameListener);
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
    pointName.setText(newValue.getName());
    nameListener = (observable, oldText, newText) -> newValue.setName(newText);
    pointName.textProperty().addListener(nameListener);
  }

  private void enableSaving(ObservableValue<Waypoint> wp) {
    // Save values when out of focus
    List.of(xPosition, yPosition, tangentX, tangentY, pointName)
        .forEach(textField -> textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
          if (!newValue && wp.getValue() != null) {
            SaveManager.getInstance().addChange(wp.getValue().getPath());
          }
        }));

    lockedTangent.selectedProperty()
        .addListener(__ -> {
          if (wp.getValue() != null) {
            SaveManager.getInstance().addChange(wp.getValue().getPath());
          }
        });
  }

  private void lockTangentOnEdit() {
    tangentY.setOnKeyTyped((KeyEvent event) -> lockedTangent.setSelected(true));
    tangentX.setOnKeyTyped((KeyEvent event) -> lockedTangent.setSelected(true));
  }
}
