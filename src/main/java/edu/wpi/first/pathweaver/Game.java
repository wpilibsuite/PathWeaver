package edu.wpi.first.pathweaver;

public enum Game {
  POWER_UP_2018("FIRST Power Up"),
  TURNING_POINT_2018_19("VEX Turning Point"); // what am i doing wrong here

  private String prettyName;

  Game(String prettyName) {
    this.prettyName = prettyName;
  }

  public String toPrettyName() {
    return prettyName;
  }

  /**
   * Returns the Game associated with a given prettyName.
   * @param prettyName The prettyName of the associated Game.
   * @return Game for prettyName.
   */
  public static Game fromPrettyName(String prettyName) {
    for (Game game : values()) {
      if (game.prettyName.equals(prettyName)) {
        return game;
      }
    }
    return null;
  }
}
