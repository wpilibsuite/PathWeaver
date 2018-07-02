package edu.wpi.first.pathui;

import javafx.util.StringConverter;

public class PathCellConverter extends StringConverter<String>{
  @Override
  public String toString(String string){
    return string;
  }

  @Override
  public java.lang.String fromString(java.lang.String string) {
    return string;
  }
}
