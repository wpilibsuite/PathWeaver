package edu.wpi.first.pathweaver;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;


// Heavily taken from StackOverflow answer by jewelsea
// https://stackoverflow.com/questions/13853621/multiple-components-in-one-column-of-javafx-tableview
public class PropertyManager {
  private final TableView<NamedProperty> propertyView;
  private Callback<PropertyEditable, Boolean> commitCallback;
  private PropertyEditable current;

  /**
   * Creates a PropertyManager with a tableview.
   * @param propertyView The tableview which to manage
   */
  public PropertyManager(TableView<NamedProperty> propertyView) {
    this.propertyView = propertyView;
    this.propertyView.setEditable(true);

    TableColumn<NamedProperty, String> nameCol = new TableColumn<>("Property");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<NamedProperty, Object> valueCol = new TableColumn<>("Value");
    valueCol.setPrefWidth(100.0);
    valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
    valueCol.setCellFactory(param -> new EditingCell());

    valueCol.setOnEditCommit(
        t -> {
          int row = t.getTablePosition().getRow();
          NamedProperty property = t.getTableView().getItems().get(row);
          Object oldVal = t.getOldValue();
          property.coerceValue(t.getNewValue());
          if (!commitCallback.call(current)) {
            property.coerceValue(oldVal);
          }
        }
    );
    this.propertyView.getColumns().add(nameCol);
    this.propertyView.getColumns().add(valueCol);
  }

  /**
   * Sets the TableView to show the editable properties of the object, or deselects editing if null is passed.
   * @param object The object to show and edit the properties of
   */
  public void manageProperties(PropertyEditable object) {
    current = object;
    if (object == null) {
      this.propertyView.setItems(null);
    } else {
      this.propertyView.setItems(object.getProperties());
    }
    this.propertyView.refresh();
  }

  /**
   * Sets the callback to be called when the PropertyManager tries to commit a property. Should validate and cleanup.
   * @param commitCallback The callback, where the first argument is the object that was just edited
   *                           and the return value is whether the edit should go ahead.
   */
  public void setCommitCallback(Callback<PropertyEditable, Boolean> commitCallback) {
    this.commitCallback = commitCallback;
  }

  public static class NamedProperty {
    private final Property value;
    private final SimpleStringProperty name = new SimpleStringProperty();

    public NamedProperty(String name, Property value) {
      this.name.set(name);
      this.value = value;
    }

    public void setValue(Object val) {
      value.setValue(val);
    }

    /**
     * Attempts to coerce val, first by setting the property, then by coercing to Double, then failing.
     * @param val The value to try to coerce.
     */
    public void coerceValue(Object val) {
      try {
        value.setValue(val);
        return;
      } catch (ClassCastException ignored) { }

      String sval = (String) val;
      try {
        value.setValue(Double.parseDouble(sval));
        return;
      } catch (ClassCastException ignored) { }

      throw new IllegalArgumentException("Failed to coerce property");
    }

    public StringProperty nameProperty() {
      return name;
    }

    public Property valueProperty() {
      return value;
    }
  }

  public interface PropertyEditable {
    ObservableList<NamedProperty> getProperties();
  }

  private class EditingCell extends TableCell<NamedProperty, Object> {
    private TextField textField;
    private CheckBox checkBox;

    public EditingCell() {
      super();
    }

    @Override public void startEdit() {
      if (!isEmpty()) {
        super.startEdit();

        if (getItem() instanceof Boolean) {
          createCheckBox();
          setText(null);
          setGraphic(checkBox);
        } else {
          createTextField();
          setText(null);
          setGraphic(textField);
          textField.selectAll();
        }
      }
    }

    @Override public void cancelEdit() {
      super.cancelEdit();
      Object item = getItem();
      if (item instanceof Boolean) {
        setText(getItem().toString());
      } else {
        setText(getItem().toString());
      }
      setGraphic(null);
    }

    @SuppressWarnings("PMD.NcssCount")
    @Override
    public void updateItem(Object item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setText(null);
        setGraphic(null);
      } else {
        if (isEditing()) {
          if (getItem() instanceof Boolean) {
            if (checkBox != null) {
              checkBox.setSelected(getBoolean());
            }
            setText(null);
            setGraphic(checkBox);
          } else {
            if (textField != null) {
              textField.setText(getString());
            }
            setText(null);
            setGraphic(textField);
          }
        } else {
          setText(getString());
          setGraphic(null);
        }
      }
    }

    private void createTextField() {
      textField = new TextField(getString());
      textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
      textField.focusedProperty().addListener((observable, oldIsFocused, newIsFocused) -> {
        if (!newIsFocused) {
          commitEdit(textField.getText());
        }
      });
      textField.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          this.requestFocus();  // Unfocus the textfield to call commitEdit
        }
      });
    }

    private void createCheckBox() {
      checkBox = new CheckBox();
      checkBox.setSelected(getBoolean());
      checkBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
      checkBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue) {
          commitEdit(checkBox.isSelected());
        }
      });
    }

    private String getString() {
      return getItem() == null ? "" : getItem().toString();
    }

    private Boolean getBoolean() {
      return getItem() != null && (Boolean) getItem();
    }
  }
}
