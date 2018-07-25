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
   *
   * @param i
   * @return
   */
  public static Color getColorForSubChild(int i) {
    Color[] colors = new Color[] {Color.ORANGE, Color.VIOLET, Color.DARKRED};
    return colors[i % colors.length];
  }

  public static boolean isSubChild(TreeView view, TreeItem item) {
    return view.getTreeItemLevel(item) == 2;
  }

  public static int getItemIndex(TreeItem item) {
    TreeItem parent = item.getParent();
    if (parent != null) {
      return parent.getChildren().indexOf(item);
    }
    return 0;
  }

}
