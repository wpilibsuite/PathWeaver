package edu.wpi.first.pathui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

@SuppressWarnings("PMD.TooManyMethods")
//With the creation of a project many of these functions should be moved out of here
//Anything to do with the directory should be part of a Project object

public class MainController {
  @FXML private TreeView<String> autons;
  @FXML private TreeView<String> paths;
  @FXML private Pane pathDisplay;
  // Variable is auto generated as Pane name + Controller
  @FXML private PathDisplayController pathDisplayController; //NOPMD

  private String directory = "pathUI/";
  private String pathDirectory;
  private String autonDirectory;
  private final TreeItem<String> autonRoot = new TreeItem<>("Autons");
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  private TreeItem<String> selected = null;

  @FXML
  private void initialize() {
    pathDirectory = directory + "Paths/";
    autonDirectory = directory + "Autons/";
    setupDrag();

    autons.setRoot(autonRoot);
    autons.getRoot().setExpanded(true);

    paths.setRoot(pathRoot);
    pathRoot.setExpanded(true);

    MainIOUtil.setupItemsInDirectory(pathDirectory, pathRoot);
    MainIOUtil.setupItemsInDirectory(autonDirectory, autonRoot);

    pathDisplayController.setPathDirectory(pathDirectory);

    setupClickablePaths();
    loadAllAutons();

    autons.setEditable(true);
    paths.setEditable(true);
    setupEditable();
  }

  @SuppressWarnings("PMD.NcssCount")
  private void setupEditable() {
    autons.setOnEditCommit((EventHandler) event -> {
      TreeView.EditEvent<String> edit = (TreeView.EditEvent<String>) event;
      if (edit.getTreeItem().getParent() == autonRoot) {
        MainIOUtil.rename(autonDirectory, edit.getTreeItem(), edit.getNewValue());
        edit.getTreeItem().setValue(edit.getNewValue());
      } else {
        MainIOUtil.rename(pathDirectory, edit.getTreeItem(), edit.getNewValue());
        renameAllPathInstances(edit.getTreeItem(), edit.getNewValue());
      }
      saveAllAutons();
      loadAllAutons();
    });
    paths.setOnEditCommit((EventHandler) event -> {
      TreeView.EditEvent<String> edit = (TreeView.EditEvent<String>) event;

      MainIOUtil.rename(pathDirectory, edit.getTreeItem(), edit.getNewValue());
      renameAllPathInstances(edit.getTreeItem(), edit.getNewValue());

      saveAllAutons();
      loadAllAutons();
      pathDisplayController.removeAllPath();
      pathDisplayController.addPath(pathDirectory, edit.getTreeItem());
    });
  }

  private void renameAllPathInstances(TreeItem<String> path, String newName) {
    String oldName = path.getValue();

    for (TreeItem<String> instance : getAllInstances(path)) {
      instance.setValue(newName);
    }
    for (TreeItem<String> potential : pathRoot.getChildren()) {
      if (oldName.equals(potential.getValue())) {
        potential.setValue(newName);
      }
    }
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
      if (selected.getParent() == autonRoot) {
        MainIOUtil.deleteItem(autonDirectory, selected);
      } else {
        removePath(selected);
      }
    } else if (pathRoot == root) {
      for (TreeItem<String> path : getAllInstances(selected)) {
        removePath(path);
      }
      MainIOUtil.deleteItem(pathDirectory, selected);
      saveAllAutons();
      loadAllAutons();
    }
  }

  @FXML
  private void keyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.DELETE
        || event.getCode() == KeyCode.BACK_SPACE) {
      delete();
    }
  }

  private List<TreeItem<String>> getAllInstances(TreeItem<String> chosenPath) {
    List<TreeItem<String>> list = new ArrayList<>();
    for (TreeItem<String> auton : autonRoot.getChildren()) {
      for (TreeItem<String> path : auton.getChildren()) {
        if (path.getValue().equals(chosenPath.getValue())) {
          list.add(path);
        }
      }
    }
    return list;
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
                pathDisplayController.addPath(pathDirectory, newValue);
              }

            });
    autons.getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              selected = newValue;
              pathDisplayController.removeAllPath();
              if (newValue != autonRoot) {
                pathDisplayController.removeAllPath();
                if (newValue.isLeaf()) { //has no children so try to display path
                  Path path = pathDisplayController.addPath(pathDirectory, newValue);
                  if (FxUtils.isSubChild(autons, newValue)) {
                    path.enableSubchildSelector(FxUtils.getItemIndex(newValue));
                  }
                } else { //is an auton with children
                  for (TreeItem<String> it : selected.getChildren()) {
                    pathDisplayController.addPath(pathDirectory, it).enableSubchildSelector(FxUtils.getItemIndex(it));
                  }
                }
              }
            });
  }

  private boolean validPathName(String oldName, String newName) {
    return MainIOUtil.isValidRename(pathDirectory, oldName, newName);
  }

  private boolean validAutonName(String oldName, String newName) {
    return MainIOUtil.isValidRename(autonDirectory, oldName, newName);
  }


  private void setupDrag() {

    paths.setCellFactory(param -> new PathCell(false, this::validPathName));
    autons.setCellFactory(param -> new PathCell(true, this::validAutonName));

    autons.setOnDragDropped(event -> {
      //simpler than communicating which was updated from the cells
      saveAllAutons();
      loadAllAutons();
    });
  }

  @FXML
  private void flipHorizontal() {
    pathDisplayController.flip(true);
  }

  @FXML
  private void flipVertical() {
    pathDisplayController.flip(false);
  }

  @FXML
  private void duplicate() {
    Path newPath = pathDisplayController.duplicate(pathDirectory);
    TreeItem<String> stringTreeItem = MainIOUtil.addChild(pathRoot, newPath.getPathName());
    PathIOUtil.export(pathDirectory, newPath);
    paths.getSelectionModel().select(stringTreeItem);
  }

  @FXML
  private void createPath() {
    String name = MainIOUtil.getValidFileName(pathDirectory, "Unnamed", ".path");
    MainIOUtil.addChild(pathRoot, name);
    Path newPath = new Path(name);
    PathIOUtil.export(pathDirectory, newPath);
  }

  @FXML
  private void createAuton() {
    String name = MainIOUtil.getValidFileName(autonDirectory, "Unnamed", "");
    TreeItem<String> auton = MainIOUtil.addChild(autonRoot, name);
    MainIOUtil.saveAuton(autonDirectory, auton.getValue(), auton);
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

