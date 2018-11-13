package edu.wpi.first.pathweaver;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javafx.scene.image.Image;

import static edu.wpi.first.pathweaver.PathUnits.FOOT;

public final class Game {

  private final String name;
  private final Field field;

  private static final Set<Game> games = new LinkedHashSet<>(); // NOPMD constant name
  public static final Game POWER_UP_2018 = create("FIRST Power Up", createPowerupField());

  private Game(String name, Field field) {
    this.name = name;
    this.field = field;
  }

  /**
   * Creates and registers a new game.
   *
   * @param name  the name of the game
   * @param field the game field
   *
   * @return the created game
   *
   * @throws DuplicateGameException if a game has already been created with the given name
   */
  public static Game create(String name, Field field) throws DuplicateGameException {
    if (games.stream().map(Game::getName).anyMatch(name::equals)) {
      throw new DuplicateGameException("A game already exists with the name \"" + name + "\"");
    }
    Game game = new Game(name, field);
    games.add(game);
    return game;
  }

  public String getName() {
    return name;
  }

  public Field getField() {
    return field;
  }

  /**
   * Gets the game with the given name, or null if no registered game has that name.
   *
   * @param name the name of the game to search for (case sensitive)
   *
   * @return the name with the given name
   */
  public static Game fromPrettyName(String name) {
    return games.stream()
        .filter(game -> game.getName().equals(name))
        .findFirst()
        .orElse(null);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Game that = (Game) obj;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public static Set<Game> getGames() {
    return games;
  }

  private static Field createPowerupField() {
    Supplier<Image> imageSupplier = () -> new Image("edu/wpi/first/pathweaver/2018-field.jpg");
    double realWidth = 54;
    double realLength = 27;
    double xPixel = 125;
    double yPixel = 20;
    double pixelWidth = 827 - xPixel;
    double pixelLength = 370 - yPixel;
    return new Field(imageSupplier, FOOT, realWidth, realLength, xPixel, yPixel, pixelWidth, pixelLength);
  }
}
