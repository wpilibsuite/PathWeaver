package edu.wpi.first.pathweaver;

import java.io.IOException;
import java.util.List;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class FxUtils { // NOPMD util class name
  private static final List<PseudoClass> SUBCHILD_SELECTORS; // NOPMD

  static {
    PseudoClass[] selectors = new PseudoClass[8];
    for (int i = 0; i < selectors.length; i++) {
      selectors[i] = PseudoClass.getPseudoClass("subchild" + i);
    }
    SUBCHILD_SELECTORS = List.of(selectors);
  }

  private FxUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Creates a menu item with the given text and event handler.
   *
   * @param text         the text of the menu item
   * @param eventHandler the handler to call when the menu item is acted upon
   */
  public static MenuItem menuItem(String text, EventHandler<ActionEvent> eventHandler) {
    MenuItem menuItem = new MenuItem(text);
    menuItem.setOnAction(eventHandler);
    return menuItem;
  }

  /**
   * Applies SUBCHILD_SELECTORS to the specified node.
   * @param node The node to apply the classes to
   */
  public static void applySubchildClasses(Node node) {
    for (PseudoClass pc : FxUtils.SUBCHILD_SELECTORS) {
      node.getStyleClass().add(pc.getPseudoClassName());
    }
  }

  /**
   * Updates all SUBCHILD_SELECTORS for the given node to be enabled (if the correct index) or disabled.
   * @param node The node to enable/disable subchild pseudoclasses on
   * @param idx The index of SUBCHILD_SELECTORS to set enabled. Use -1 to disable all.
   */
  public static void enableSubchildSelector(Node node, int idx) {
    for (int i = 0; i < SUBCHILD_SELECTORS.size(); i++) {
      node.pseudoClassStateChanged(SUBCHILD_SELECTORS.get(i), i == idx % SUBCHILD_SELECTORS.size());
    }
  }

  /**
   * Checks whether the item is a child of a child (exactly the child of a child of the root).
   * @param view The treeview to check against
   * @param item The item to check
   * @return Whether this is a sub child
   */
  public static boolean isSubChild(TreeView view, TreeItem item) {
    return view.getTreeItemLevel(item) == 2;
  }

  /**
   * Gets the index of this item relative to its siblings.
   * @param item The item to check
   * @return The child index of this item, 0 if it has no parent.
   */
  public static int getItemIndex(TreeItem item) {
    TreeItem parent = item.getParent();
    if (parent != null) {
      return parent.getChildren().indexOf(item);
    }
    return 0;
  }

  public static void loadMainScreen(Scene scene, Class aClass) {
    try {
      Pane root = FXMLLoader.load(aClass.getResource("main.fxml"));
      Stage primaryStage = (Stage) scene.getWindow();
      primaryStage.resizableProperty().setValue(true);
      ProgramPreferences.getInstance().setResolution(primaryStage);
      scene.getStylesheets().add("/edu/wpi/first/pathweaver/style.css");
      scene.setRoot(root);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
