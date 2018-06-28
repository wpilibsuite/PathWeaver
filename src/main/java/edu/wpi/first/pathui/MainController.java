package edu.wpi.first.pathui;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;


public class MainController {
  @FXML private TreeView<String> autons;
  @FXML private TreeView<String> paths;
  @FXML private Pane pathDisplay;
  // Variable is auto generated as Pane name + Controller
  @FXML private PathDisplayController pathDisplayController; //NOPMD

  private String directory = "pathUI/";
  private String pathDirectory;
  private String autonDirectory;
  private final TreeItem<String> autonRoot = new TreeItem<String>("Autons");
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  private TreeItem<String> selected = null;

  @FXML
  private void initialize() {
    pathDirectory = directory + "Paths/";
    autonDirectory = directory + "Autons/";
    setupDrag(paths, false);
    setupDrag(autons, true);

    autons.setRoot(autonRoot);
    autons.getRoot().setExpanded(true);

    paths.setRoot(pathRoot);
    pathRoot.setExpanded(true);

    MainIOUtil.setupItemsInDirectory(pathDirectory, pathRoot);
    MainIOUtil.setupItemsInDirectory(autonDirectory, autonRoot);

    pathDisplayController.setPathDirectory(pathDirectory);

    setupClickablePaths();
    loadAllAutons();
  }

  private void loadAllAutons() {
    for (TreeItem<String> item : autonRoot.getChildren()) {
      MainIOUtil.loadAuton(autonDirectory, item.getValue(), item);
    }
  }

  private void saveAllAutons() {
    for (TreeItem<String> item : autonRoot.getChildren()) {
      MainIOUtil.saveAuton(autonDirectory, item.getValue(), item);
    }
  }

  @FXML
  private void delete() {
    if (selected == null) {
      // have nothing selected
      return;
    }
    TreeItem<String> root = getRoot(selected);
    if (selected == root) {
      // clicked impossible thing to delete
      return;
    }
    if (autonRoot == root) {
      if (selected.getParent().getParent() == null) {
        MainIOUtil.deleteItem(autonDirectory, selected);
      } else {
        removePath(selected);
      }
    } else if (pathRoot == root) {
      deletePath(selected);
      saveAllAutons();
      loadAllAutons();
    }
  }

  @FXML
  private void keyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.DELETE) {
      delete();
    }
    if (event.getCode() == KeyCode.BACK_SPACE) {
      delete();
    }
  }

  private void deletePath(TreeItem<String> pathDelete) {
    ArrayList<TreeItem<String>> deleteList = new ArrayList<>();
    for (TreeItem<String> auton : autonRoot.getChildren()) {
      for (TreeItem<String> path : auton.getChildren()) {
        if (path.getValue().equals(pathDelete.getValue())) {
          deleteList.add(path);
        }
      }
    }
    for (TreeItem<String> path : deleteList) {
      removePath(path);
    }
    MainIOUtil.deleteItem(pathDirectory, pathDelete);
  }

  private void removePath(TreeItem<String> path) {
    TreeItem<String> auton = path.getParent();
    auton.getChildren().remove(path);
    MainIOUtil.saveAuton(autonDirectory, auton.getValue(), auton);
  }


  private TreeItem<String> getRoot(TreeItem<String> item) {
    TreeItem<String> root = item;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return root;
  }


  private void setupClickablePaths() {
    paths.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              selected = newValue;
              if (newValue == pathRoot) {
                //pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                pathDisplayController.removeAllPath();
                pathDisplayController.addPath(pathDirectory, newValue.getValue());
              }

            });
    autons.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              selected = newValue;
              if (newValue == autonRoot) {
                //pathRoot.setExpanded(!pathRoot.isExpanded());
              } else {
                pathDisplayController.removeAllPath();
                for (TreeItem<String> item : newValue.getChildren()) {
                  pathDisplayController.addPath(pathDirectory, item.getValue());

                }
              }
            });

  }

  private void setupDrag(TreeView<String> tree, boolean validDropTarget) {
    tree.setCellFactory(param -> new PathCell(validDropTarget));
    autons.setOnDragDropped(event -> {
      //simpler than communicating which was updated from the cells
      saveAllAutons();
      loadAllAutons();
    });
  }

  @FXML
  private void createPath() {
    MainIOUtil.addChild(pathRoot, "Unnamed");
  }

  @FXML
  private void createAuton() {
    MainIOUtil.addChild(autonRoot, "Unnamed");
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

