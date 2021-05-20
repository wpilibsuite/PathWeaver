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
public class EditAutoController {
    @FXML
    private Button rectangle;
    @FXML
    private Button ellipse;
    @FXML
    private ListView autoView;

    private List<Control> controls;
    private ChangeListener<String> nameListener;

    @FXML
    private void initialize() {
        controls = List.of(rectangle, ellipse, autoView);
        controls.forEach(control -> control.setDisable(true));
        //List<TextField> textFields = List.of(xPosition, yPosition, tangentX, tangentY);
        //textFields.forEach(textField -> textField.setTextFormatter(FxUtils.onlyDoubleText()));
    }

    @FXML
    private void createRectangle() {

    }

    @FXML
    private void createEllipse() {

    }
}
