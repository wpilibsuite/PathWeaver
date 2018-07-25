package edu.wpi.first.pathui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

public final class FxUtils { // NOPMD util class name

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
   * Get the color according to sub-child order.
   * @param i The index of the child relative to the parent
   * @return The color to draw the thing as
   */
  public static Color getColorForSubChild(int i) {
    Color[] colors = new Color[]{Color.ORANGE, Color.VIOLET, Color.FUCHSIA};
    return colors[i % colors.length];
  }

  /**
   * Check whether the item is a child of a child (exactly the child of a child of the root.)
   * @param view The treeview to check against
   * @param item The item to check
   * @return Whether this is a sub child
   */
  public static boolean isSubChild(TreeView view, TreeItem item) {
    return view.getTreeItemLevel(item) == 2;
  }

  /**
   * Get the index of this item relative to its siblings.
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
