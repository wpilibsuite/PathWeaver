package edu.wpi.first.pathweaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.modifiers.TankModifier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
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

  @FXML private GridPane editWaypoint;
  // Variable is auto generated as Pane name + Controller
  @FXML private EditWaypointController editWaypointController; //NOPMD

  private String directory = ProjectPreferences.getInstance().getDirectory();
  private final String pathDirectory = directory + "/Paths/";
  private final String autonDirectory = directory + "/Autons/";
  private final String outputDirectory = directory + "/output/";
  private final TreeItem<String> autonRoot = new TreeItem<>("Autons");
  private final TreeItem<String> pathRoot = new TreeItem<>("Paths");

  private TreeItem<String> selected = null;

  @FXML private Button duplicate;
  @FXML private Button flipHorizontal;
  @FXML private Button flipVertical;

  @FXML
  private void initialize() {
    setupDrag();

    setupTreeView(autons, autonRoot, FxUtils.menuItem("New Autonomous...", event -> createAuton()));

    setupTreeView(paths, pathRoot, FxUtils.menuItem("New Path...", event -> createPath()));

    MainIOUtil.setupItemsInDirectory(pathDirectory, pathRoot);
    MainIOUtil.setupItemsInDirectory(autonDirectory, autonRoot);

    pathDisplayController.setPathDirectory(pathDirectory);

    setupClickablePaths();
    setupClickableAutons();
    loadAllAutons();

    autons.setEditable(true);
    paths.setEditable(true);
    setupEditable();

    duplicate.disableProperty().bind(pathDisplayController.currentPathProperty().isNull());
    flipHorizontal.disableProperty().bind(pathDisplayController.currentPathProperty().isNull());
    flipVertical.disableProperty().bind(pathDisplayController.currentPathProperty().isNull());

    editWaypointController.bindToWaypoint(pathDisplayController.selectedWaypointProperty(), pathDisplayController);
  }

  private void setupTreeView(TreeView<String> treeView, TreeItem<String> treeRoot, MenuItem newItem) {
    treeView.setRoot(treeRoot);
    treeView.setContextMenu(new ContextMenu());
    treeView.getContextMenu().getItems().addAll(newItem, FxUtils.menuItem("Delete", event -> delete()));
    treeRoot.setExpanded(true);
    treeView.setShowRoot(false); // Don't show the roots "Paths" and "Autons" - cleaner appearance
  }

  @SuppressWarnings("PMD.NcssCount")
  private void setupEditable() {
    autons.setOnEditStart(event -> {
      if (event.getTreeItem().getParent() != autonRoot) {
        SaveManager.getInstance().promptSaveAll(false);
      }
    });
    autons.setOnEditCommit(event -> {
      if (event.getTreeItem().getParent() == autonRoot) {
        MainIOUtil.rename(autonDirectory, event.getTreeItem(), event.getNewValue());
        event.getTreeItem().setValue(event.getNewValue());
      } else {
        MainIOUtil.rename(pathDirectory, event.getTreeItem(), event.getNewValue());
        renameAllPathInstances(event.getTreeItem(), event.getNewValue());
      }
      saveAllAutons();
      loadAllAutons();
    });
    paths.setOnEditStart(event -> SaveManager.getInstance().promptSaveAll(false));
    paths.setOnEditCommit(event -> {
      MainIOUtil.rename(pathDirectory, event.getTreeItem(), event.getNewValue());
      renameAllPathInstances(event.getTreeItem(), event.getNewValue());

      saveAllAutons();
      loadAllAutons();
      pathDisplayController.removeAllPath();
      pathDisplayController.addPath(pathDirectory, event.getTreeItem());
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
    ChangeListener<TreeItem<String>> selectionListener =
        new ChangeListener<>() {
          @Override
          public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,
                              TreeItem<String> newValue) {
            if (!SaveManager.getInstance().promptSaveAll()) {
              paths.getSelectionModel().selectedItemProperty().removeListener(this);
              paths.getSelectionModel().select(oldValue);
              paths.getSelectionModel().selectedItemProperty().addListener(this);
              return;
            }
            selected = newValue;
            if (newValue != pathRoot) {
              pathDisplayController.removeAllPath();
              pathDisplayController.addPath(pathDirectory, newValue);
            }
          }
        };
    paths.getSelectionModel().selectedItemProperty().addListener(selectionListener);
  }

  @SuppressWarnings("PMD.NcssCount")
  private void setupClickableAutons() {
    ChangeListener<TreeItem<String>> selectionListener = new ChangeListener<>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,
                          TreeItem<String> newValue) {
        if (newValue == null) {
          return;
        }
        if (!SaveManager.getInstance().promptSaveAll()) {
          autons.getSelectionModel().selectedItemProperty().removeListener(this);
          autons.getSelectionModel().select(oldValue);
          autons.getSelectionModel().selectedItemProperty().addListener(this);
          return;
        }
        selected = newValue;
        pathDisplayController.removeAllPath();
        if (newValue != autonRoot) {
          if (newValue.getParent() == autonRoot) { //is an auton with children
            for (TreeItem<String> it : selected.getChildren()) {
              pathDisplayController.addPath(pathDirectory, it).enableSubchildSelector(FxUtils.getItemIndex(it));
            }
          } else { //has no children so try to display path
            Path path = pathDisplayController.addPath(pathDirectory, newValue);
            if (FxUtils.isSubChild(autons, newValue)) {
              path.enableSubchildSelector(FxUtils.getItemIndex(newValue));
            }
          }
        }
      }
    };
    autons.getSelectionModel().selectedItemProperty().addListener(selectionListener);
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
    SaveManager.getInstance().saveChange(newPath);
    paths.getSelectionModel().select(stringTreeItem);
  }

  @FXML
  private void createPath() {
    String name = MainIOUtil.getValidFileName(pathDirectory, "Unnamed", ".path");
    MainIOUtil.addChild(pathRoot, name);
    Path newPath = new Path(name);
    SaveManager.getInstance().saveChange(newPath);
  }

  @FXML
  private void createAuton() {
    String name = MainIOUtil.getValidFileName(autonDirectory, "Unnamed", "");
    TreeItem<String> auton = MainIOUtil.addChild(autonRoot, name);
    MainIOUtil.saveAuton(autonDirectory, auton.getValue(), auton);
  }

  @FXML
  private void buildPaths() {
    if(!SaveManager.getInstance().promptSaveAll()) {
      return;
    }
    new File(outputDirectory).mkdir();
    for (TreeItem<String> pathName : pathRoot.getChildren()) {
      Path path = PathIOUtil.importPath(pathDirectory, pathName.getValue());
      TankModifier tank = path.getTankModifier();
      Pathfinder.writeToCSV(new File(outputDirectory + path.getPathNameNoExtension() + "_left.csv"),
          tank.getLeftTrajectory());
      Pathfinder.writeToCSV(new File(outputDirectory + path.getPathNameNoExtension() + "_right.csv"),
          tank.getRightTrajectory());
    }
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }
}

