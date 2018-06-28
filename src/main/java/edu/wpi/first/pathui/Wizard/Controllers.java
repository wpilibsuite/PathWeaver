package edu.wpi.first.pathui.wizard;

import javafx.beans.property.BooleanProperty;

public interface Controllers {

   BooleanProperty getReadyForNext();

  public WizardController.Panes getNextPane();

  void storeInfo();

}
