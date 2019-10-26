package edu.wpi.first.pathweaver;

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
  private final TreeCell<String> cell;
  private final boolean validDropTarget;
  private boolean editing = false;
  //having a single TEMP_ITEM you use for all dragging
  //means that any dragover call is able to remove the temporary
  //item from past locations
  //each cell might have its own dragover call as you move around
  private static final TreeItem<String> TEMP_ITEM = new TreeItem<>("");
  private static TreeItem<String> dragSource = null;
  private static int dragOriginalIndex = 0;


  private final BiFunction<String, String, Boolean> renameIsValid;
  private TextField text;

  /**
   * Creates PathCell, a TreeCell object that can be dragged and used as a drag target.
   *
   * @param validDropTarget if this item should allow drag over and drag drop.
   * @param validation a bifunction that checks if renaming the cell is valid
   */
  public PathCell(boolean validDropTarget, BiFunction<String, String, Boolean> validation) {
    super();
    cell = this;
    setupDragStart();
    this.validDropTarget = validDropTarget;
    renameIsValid = validation;
    if (validDropTarget) {
      setupDragOver();
      setupDragDrop();
    }
    this.setConverter(new DefaultStringConverter());
    FxUtils.applySubchildClasses(this);
  }

  @Override
  public void updateItem(String s, boolean b) {
    super.updateItem(s, b);
    updateColor();
  }

  @Override
  public void updateIndex(int i) {
    super.updateIndex(i);
    updateColor();
  }

  private void updateColor() {
    if (FxUtils.isSubChild(this.getTreeView(), this.getTreeItem())) {
      FxUtils.enableSubchildSelector(this, FxUtils.getItemIndex(this.getTreeItem()));
    } else {
      FxUtils.enableSubchildSelector(this, -1);
    }
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
      if (TEMP_ITEM.getValue().equals("")) {
        //Was dragging within auton
        this.getTreeView().getSelectionModel().select(this.getTreeItem().getParent());
      } else {
        TreeItem<String> newItem = new TreeItem<>(TEMP_ITEM.getValue()); //make new item

        int index = TEMP_ITEM.getParent().getChildren().indexOf(TEMP_ITEM); //get location of old item
        TEMP_ITEM.getParent().getChildren().add(index, newItem); // add new item to temp item location

        TEMP_ITEM.getParent().getChildren().remove(TEMP_ITEM); //reset TEMP_ITEM
        TEMP_ITEM.setValue("");
        this.getTreeView().getSelectionModel().select(newItem.getParent());
      }
    });
  }

  private void setupDragStart() {
    this.setOnDragDetected(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null && item.isLeaf()) {
        // Don't allow dragging auton onto another auton
        if (validDropTarget && item.getParent().getParent() == null) {
          return;
        }
        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putString(item.getValue());
        db.setContent(content);
        event.consume();
        dragSource = item;
        dragOriginalIndex = item.getParent().getChildren().indexOf(item);
      }
    });
  }


  private void setupDragOver() {

    cell.setOnDragOver(event -> {
      TreeItem<String> item = cell.getTreeItem();
      if (item != null) {
        TreeItem<String> parent = item.getParent();

        if (parent != null
            && event.getDragboard().hasString()) {
          Dragboard db = event.getDragboard();
          String source = db.getString();

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
    if (parent == dragSource.getParent()) {
      //drag same place
      parent.getChildren().remove(dragSource);
      parent.getChildren().add(currentIndex, dragSource);
      cell.getTreeView().getSelectionModel().select(dragSource);

    } else {
      parent.getChildren().add(currentIndex, TEMP_ITEM);

      if (dragSource.getParent().getChildren().indexOf(dragSource) != dragOriginalIndex) {
        TreeItem<String> sourceParent = dragSource.getParent();
        sourceParent.getChildren().remove(dragSource);
        sourceParent.getChildren().add(dragOriginalIndex, dragSource);
        //put it back
      }
      TEMP_ITEM.setValue(source);
      cell.getTreeView().getSelectionModel().select(TEMP_ITEM);

    }

  }

  private void removeTemp() {
    TreeItem<String> tempParent = TEMP_ITEM.getParent();
    if (tempParent != null) {
      tempParent.getChildren().remove(TEMP_ITEM);
    }
  }

}



