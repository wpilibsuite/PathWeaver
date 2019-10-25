package edu.wpi.first.pathweaver.extensions;

import edu.wpi.first.pathweaver.DuplicateGameException;
import edu.wpi.first.pathweaver.Game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ExtensionManager {
  private static final Logger LOGGER = Logger.getLogger(ExtensionManager.class.getName());
  private static final ExtensionManager INSTANCE = new ExtensionManager();

  private final String directory = System.getProperty("user.home") + "/PathWeaver/Games";
  private final ExtensionLoader loader = new ExtensionLoader();
  private final List<Game> games = new ArrayList<>();

  private ExtensionManager() {
    new File(directory).mkdirs();
  }

  public static ExtensionManager getInstance() {
    return INSTANCE;
  }

  private List<Game> findGames() throws IOException {
    List<Game> games = new ArrayList<>();

    Files.list(Paths.get(directory))
        .filter(Files::isDirectory)
        .map(this::loadGameFromDir)
        .flatMap(Optional::stream)
        .forEach(games::add);

    scanForZips().stream()
        .map(this::loadGameFromZip)
        .flatMap(Optional::stream)
        .forEach(games::add);
    return games;
  }

  private Optional<Game> loadGameFromDir(Path dir) {
    try {
      return Optional.of(loader.loadFromDir(dir));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not load game from " + dir.toAbsolutePath(), e);
    } catch (DuplicateGameException e) {
      LOGGER.warning("Duplicate game name in " + dir.toAbsolutePath() + ": " + e.getMessage());
    } catch (RuntimeException e) { // NOPMD
      LOGGER.log(Level.WARNING, "General exception when loading from " + dir.toAbsolutePath(), e);
    }
    return Optional.empty();
  }

  private Optional<Game> loadGameFromZip(Path zip) {
    try {
      return Optional.ofNullable(loader.loadFromZip(zip));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not load game from ZIP file " + zip.toAbsolutePath(), e);
    } catch (DuplicateGameException e) {
      LOGGER.warning("Duplicate game name in ZIP file " + zip.toAbsolutePath() + ": " + e.getMessage());
    } catch (RuntimeException e) { // NOPMD
      LOGGER.log(Level.WARNING, "General exception when loading from ZIP file " + zip.toAbsolutePath(), e);
    }
    return Optional.empty();
  }

  private List<Path> scanForZips() throws IOException {
    return Files.list(Paths.get(directory))
        .filter(path -> path.toString().endsWith(".zip"))
        .collect(Collectors.toList());
  }

  /**
   * Gets the list of discovered games. The list will be empty until {@link #refresh()} is called.
   */
  public List<Game> getGames() {
    return games;
  }

  /**
   * Refreshes the list of discovered games.
   */
  public List<Game> refresh() {
    try {
      List<Game> foundGames = findGames();
      games.clear();
      games.addAll(foundGames);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Could not refresh game extensions", e);
    }
    return games;
  }
}
