package edu.wpi.first.pathui;

import java.util.function.BiFunction;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.util.converter.DefaultStringConverter;


/**
 * TreeItem with support for dragging.
 */
public class PathCell extends TextFieldTreeCell<String> {
  private final TreeCell cell;
  private boolean editing = false;
  //having a single EMPTY_ITEM you use for all dragging
  //means that any dragover call is able to remove the temporary
  //item from past locations
  //each cell might have its own dragover call as you move around
  private static final TreeItem<String> EMPTY_ITEM = new TreeItem<>("");
  private final BiFunction<String, String, Boolean> renameIsValid;
  private TextField text;

  /**
   * Creates PathCell, a TreeCell object that can be dragged and used as a drag target.
   *
   * @param validDropTarget If this item should allow drag over and drag drop.
   */
  public PathCell(boolean validDropTarget, BiFunction<String, String, Boolean> validation) {
    super();
    cell = this;
    setupDragStart();
    renameIsValid = validation;
    if (validDropTarget) {
      setupDragOver();
      setupDragDrop();
    }
    this.setConverter(new DefaultStringConverter());
  }

  @Override
  public void startEdit() {
    editing = true;
    super.startEdit();
    Node node = getGraphic();
    if (node instanceof TextField) {
      text = (TextField) node;
      text.focusedProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue && editing) {
          this.commitEdit(text.getText());

        }
      });
      text.setOnKeyPressed(e -> {
        if (e.getCode().equals(KeyCode.ESCAPE)) {
          editing = false;
          super.cancelEdit();
        }
      });
    }
  }

  @Override
  public void commitEdit(String newValue) {
    if (!editing) {
      return;
    }
    if (renameIsValid.apply(this.getTreeItem().getValue(), newValue)) {
      super.commitEdit(newValue);
    } else {
      editing = false;
      Alert a = new Alert(Alert.AlertType.INFORMATION);
      a.setTitle("");
      a.setHeaderText("The item could not be renamed.");
      String content = String.format("The name \"%s\" is already used in this location. \n"
          + "Please use a different name.", newValue);
      a.setContentText(content);

      a.showAndWait();
      super.cancelEdit();
    }
  }

  @Override
  public void cancelEdit() {
    //super.cancelEdit();
    //Let KeyPress trigger super.cancelEdit() directly instead.
    //Allows different reasons for calling cancelEdit to trigger different behavior
  }

  private void setupDragDrop() {
    this.setOnDragDropped(event -> {
      TreeItem<String> newItem = new TreeItem<>(EMPTY_ITEM.getValue()); //make new item

      int index = EMPTY_ITEM.getParent().getChildren().indexOf(EMPTY_ITEM); //get location of old item
      EMPTY_ITEM.getParent().getChildren().add(index, newItem); // add new item to temp item location

      EMPTY_ITEM.getParent().getChildren().remove(EMPTY_ITEM); //reset EMPTY_ITEM
      EMPTY_ITEM.setValue("");
    });
  }

  private void setupDragStart() {
    this.setOnDragDetected(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null && item.isLeaf()) {
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putString(item.getValue());
        db.setContent(content);
        event.consume();
      }
    });
  }


  private void setupDragOver() {

    cell.setOnDragOver(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null) {
        TreeItem<String> parent = item.getParent();

        if (parent != null
            && event.getGestureSource() != cell
            && event.getDragboard().hasString()) {
          Dragboard db = event.getDragboard();
          String source = db.getString();

          //TODO check if duplicate
          if (item.isLeaf() && item.getParent().getParent() != null) {
            int currentIndex = parent.getChildren().indexOf(item);
            event.acceptTransferModes(TransferMode.COPY);
            //place it
            parent.setExpanded(true);
            setTemp(source, parent, currentIndex);
          } else {
            item.setExpanded(true);
            event.acceptTransferModes(TransferMode.COPY);
            setTemp(source, item, 0);
          }
          event.consume();

        }
      }
    });
  }

  private void setTemp(String source, TreeItem<String> parent, int currentIndex) {
    removeTemp();
    if (currentIndex >= 0) {
      parent.getChildren().add(currentIndex, EMPTY_ITEM);
      EMPTY_ITEM.setValue(source);
    }
  }

  private void removeTemp() {
    TreeItem<String> tempParent = EMPTY_ITEM.getParent();
    if (tempParent != null) {
      tempParent.getChildren().remove(EMPTY_ITEM);
    }
  }

}



