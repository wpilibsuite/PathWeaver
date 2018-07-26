package edu.wpi.first.pathui;

import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

public final class FxUtils { // NOPMD util class name
  public static final PseudoClass[] SUBCHILD_CLASSES = new PseudoClass[8];

  static {
    for (int i = 0; i < SUBCHILD_CLASSES.length; i++) {
      SUBCHILD_CLASSES[i] = PseudoClass.getPseudoClass("subchild" + i);
    }
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
   * Gets the color according to sub-child order.
   * @param i The index of the child relative to the parent
   * @return The color to draw the thing as
   */
  public static PseudoClass getClassForSubChild(int i) {
    return SUBCHILD_CLASSES[i % SUBCHILD_CLASSES.length];
  }

  // todo javadoc
  public static void applySubchildClasses(Node node) {
    for (PseudoClass pc : FxUtils.SUBCHILD_CLASSES) {
      node.getStyleClass().add(pc.getPseudoClassName());
    }
    node.getStyleClass().add("subchild");
  }

  public static void enableSubchildClass(Node node, int idx) {
    for (int i = 0; i < SUBCHILD_CLASSES.length; i++) {
      node.pseudoClassStateChanged(SUBCHILD_CLASSES[i], i == idx);
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

}
