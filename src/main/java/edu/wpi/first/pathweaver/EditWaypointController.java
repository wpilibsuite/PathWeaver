package edu.wpi.first.pathweaver;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.util.concurrent.Callable;

public class EditWaypointController {
    @FXML private TextField xPosition;
    @FXML private TextField yPosition;
    @FXML private TextField tangentX;
    @FXML private TextField tangentY;
    @FXML private CheckBox lockedTangent;
    @FXML private TextField pointName;


    private ObjectProperty<Point2D> tangent = new SimpleObjectProperty<>();

    @FXML
    private void initialize() {

    }

    public void bindToWaypoint(ObservableValue<Waypoint> wp) {
        wp.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                disableDoubleBinding(xPosition, oldValue.xProperty());
                disableDoubleBinding(yPosition, oldValue.yProperty());
                tangent.unbindBidirectional(oldValue.tangentProperty());
                lockedTangent.selectedProperty().unbindBidirectional(oldValue.lockTangentProperty());
                lockedTangent.setSelected(false);
                pointName.textProperty().unbindBidirectional(oldValue.nameProperty());
            } else {
                enableDoubleBinding(xPosition, newValue.xProperty());
                enableDoubleBinding(yPosition, newValue.yProperty());
                tangent.bindBidirectional(newValue.tangentProperty());
                lockedTangent.selectedProperty().bindBidirectional(newValue.lockTangentProperty());
                pointName.textProperty().bindBidirectional(newValue.nameProperty());
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
}
