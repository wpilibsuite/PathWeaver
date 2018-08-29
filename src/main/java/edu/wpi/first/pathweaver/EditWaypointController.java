package edu.wpi.first.pathweaver;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import java.util.List;


public class EditWaypointController {
    @FXML private TextField xPosition;
    @FXML private TextField yPosition;
    @FXML private TextField tangentX;
    @FXML private TextField tangentY;
    @FXML private CheckBox lockedTangent;
    @FXML private TextField pointName;

    private List<Control> controls;

    @FXML
    private void initialize() {
        controls = List.of(xPosition, yPosition, tangentX, tangentY, lockedTangent, pointName);
        controls.forEach(control -> control.setDisable(true));
    }

    public void bindToWaypoint(ObservableValue<Waypoint> wp) {
        wp.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                unbind(oldValue);
            }
            if (newValue != null) {
                bind(newValue);
            }
        });
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
        }
        enableDoubleBinding(xPosition, newValue.xProperty());
        enableDoubleBinding(yPosition, newValue.yProperty());
        enableDoubleBinding(tangentX, newValue.tangentXProperty());
        enableDoubleBinding(tangentY, newValue.tangentYProperty());
        lockedTangent.selectedProperty().bindBidirectional(newValue.lockTangentProperty());
        pointName.textProperty().bindBidirectional(newValue.nameProperty());
    }
}
