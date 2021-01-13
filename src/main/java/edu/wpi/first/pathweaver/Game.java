package edu.wpi.first.pathweaver;

import edu.wpi.first.pathweaver.extensions.ExtensionLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javafx.scene.image.Image;

public final class Game {
  private static final Set<Game> GAMES = new LinkedHashSet<>();
  public static final Game INFINTE_RECHARGE_2020 = loadGameFromResource("2020-infiniterecharge.json");
  public static final Game DEEP_SPACE_2019 = loadGameFromResource("2019-deepspace.json");
  public static final Game POWER_UP_2018 = loadGameFromResource("2018-powerup.json");
  public static final Game BARREL_RACING_PATH = loadGameFromResource("barrelracingpath.json");
  public static final Game BOUNCE_PATH = loadGameFromResource("bouncepath.json");
  public static final Game GALACTIC_SEARCH_A = loadGameFromResource("galacticsearcha.json");
  public static final Game GALACTIC_SEARCH_B = loadGameFromResource("galacticsearchb.json");
  public static final Game SLALOM_PATH = loadGameFromResource("slalompath.json");

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

  private static Game loadGameFromResource(String gameJsonPath) {
    String jsonText;
    try (var reader = new InputStreamReader(Game.class.getResourceAsStream(gameJsonPath))) {
      StringWriter writer = new StringWriter();
      reader.transferTo(writer);
      jsonText = writer.toString();
      writer.close();
    } catch (IOException e) {
      throw new IllegalStateException("Could not load the resource game definition: " + gameJsonPath, e);
    }
    ExtensionLoader loader = new ExtensionLoader();
    return loader.loadFromJsonString(name -> new Image(Game.class.getResourceAsStream(name)), jsonText);
  }
}
