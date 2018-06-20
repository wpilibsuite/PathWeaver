package edu.wpi.first.pathui;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class PathCell extends TreeCell<String> {
  private TreeCell cell;

  //having a single tempItem you use for all dragging
  //means that any dragover call is able to remove the temporary
  //item from past locations
  //each cell might have its own dragover call as you move around
  private static TreeItem<String> tempItem = new TreeItem<>("");

  public PathCell(boolean validDropTarget) {
    cell = this;
    setupDragStart();

    if(validDropTarget){
      setupDragOver();
      setupDragDrop();
    }
  }

  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    if (item != null) {
      setText(item);
    } else {
      setText(null);
    }
  }
  private void setupDragDrop(){
    this.setOnDragDropped(event -> {
      //reset temp and leave old temp where it is
      tempItem = new TreeItem<>("");
      System.out.println("dropped");
      //should also write to file
    });



  }

  private void setupDragStart() {
    this.setOnDragDetected(event -> {
      TreeItem<String> item = cell.getTreeItem();
      System.out.println("drag started");
      System.out.println(item);
      System.out.println("first " + item!=null + " second " + item.isLeaf());
      if (item != null && item.isLeaf()) {
        System.out.println("drag succeeded");
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
      if(item != null) {
        TreeItem<String> parent = item.getParent();

        if (parent != null &&
            event.getGestureSource() != cell &&
            event.getDragboard().hasString()) {
          String target = ((TreeItem<String>) cell.getTreeItem()).getValue();
          Dragboard db = event.getDragboard();
          String source = db.getString();

          //TODO check if duplicate
          if (item.isLeaf()) {
            int currentIndex = parent.getChildren().indexOf(item);
            event.acceptTransferModes(TransferMode.COPY);

            //place it
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
      parent.getChildren().add(currentIndex, tempItem);
      tempItem.setValue(source);
    }
  }

  private void removeTemp() {
    TreeItem<String> tempParent = tempItem.getParent();
    if (tempParent != null) {
      tempParent.getChildren().remove(tempItem);
    }
  }

}



