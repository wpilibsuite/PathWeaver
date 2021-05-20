package edu.wpi.first.pathweaver;

import java.util.List;

import edu.wpi.first.pathweaver.global.CurrentSelections;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.NumberStringConverter;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class EditPathController {
    @FXML
    private Label velocityLabel;
    @FXML
    private Label accelerationLabel;
    @FXML
    private TextField maxVelocity;
    @FXML
    private TextField maxAcceleration;
    @FXML
    private Label velocityUnits;
    @FXML
    private Label accelerationUnits;
    @FXML
    private CheckBox reverseSpline;
    @FXML
    private RadioButton quinticButton;
    @FXML
    private RadioButton cubicButton;


    private List<Control> controls;
    private ChangeListener<String> nameListener;

    @FXML
    private void initialize() {
        controls = List.of(maxVelocity, maxAcceleration, reverseSpline);
        controls.forEach(control -> control.setDisable(true));
        List<TextField> textFields = List.of(maxVelocity, maxAcceleration);
        textFields.forEach(textField -> textField.setTextFormatter(FxUtils.onlyDoubleText()));
        ToggleGroup group = new ToggleGroup();
        quinticButton.setToggleGroup(group);
        cubicButton.setToggleGroup(group);
    }

}
