package edu.wpi.first.pathweaver;

import edu.wpi.fields.Fields;
import edu.wpi.first.pathweaver.extensions.ExtensionLoader;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class Game {
  private static final Set<Game> GAMES = new LinkedHashSet<>();
  public static final Game DEFAULT_GAME;

  static {
    Game defaultGame = null;
    for (Fields fieldType : Fields.values()) {
      try {
        ExtensionLoader loader = new ExtensionLoader();
        Game g = loader.loadPredefinedField(fieldType);
        if (fieldType == Fields.kDefaultField) {
          defaultGame = g;
        }
      } catch (IOException e) {
        throw new RuntimeException(e); // NOPMD(AvoidThrowingRawExceptionTypes)
      }
    }

    Objects.requireNonNull(defaultGame);
    DEFAULT_GAME = defaultGame;
  }

  private final String name;
  private final Field field;

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
   */
  public static Game create(String name, Field field) {
    if (GAMES.stream().map(Game::getName).anyMatch(name::equals)) {
      throw new DuplicateGameException("A game already exists with the name \"" + name + "\"");
    }
    Game game = new Game(name, field);
    GAMES.add(game);
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
    return GAMES.stream()
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
    return GAMES;
  }
}
