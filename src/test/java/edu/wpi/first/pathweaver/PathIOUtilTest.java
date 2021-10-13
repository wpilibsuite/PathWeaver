package edu.wpi.first.pathweaver;

import edu.wpi.first.pathweaver.path.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

public class PathIOUtilTest {
    private static final double EPSILON = 1e-5;

    private String pathDirectory;
    private String projectDirectory;

    @BeforeEach
    public void initialize(@TempDir java.nio.file.Path temp) throws IOException {
        Files.createDirectories(temp.resolve("Paths/"));
        projectDirectory = temp.toAbsolutePath().toString();
        pathDirectory = temp.resolve("Paths/").toAbsolutePath().toString();
    }

    @Test
    public void loadLegacyFileMeters() throws IOException {
        String legacyFileName = "MetersLegacyPath.csv";
        String expectedFileName = "MetersExpectedPath.json";
        String pathweaverFile = "MetersPathweaverConfig.json";
        setupConfigFiles(legacyFileName, pathweaverFile);
        ProjectPreferences.getInstance(projectDirectory);

        // We expect the legacy path to exist
        assertTrue(Files.exists(Paths.get(pathDirectory, legacyFileName)));

        Path path = PathIOUtil.importPath(pathDirectory, legacyFileName);
        assertNotNull(path);
        assertEquals(6, path.getWaypoints().size());

        // After loading, the legacy version should be deleted
        assertFalse(Files.exists(Paths.get(pathDirectory, legacyFileName)));


        // Check that the points match
        {
            Waypoint waypoint = path.getWaypoints().get(0);
            assertEquals(0.1, waypoint.getX(), EPSILON);
            assertEquals(-7.01, waypoint.getY(), EPSILON);
            assertEquals(10.5, waypoint.getTangentX(), EPSILON);
            assertEquals(0, waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(1);
            assertEquals(7, waypoint.getX(), EPSILON);
            assertEquals(-4.2, waypoint.getY(), EPSILON);
            assertEquals(7.6, waypoint.getTangentX(), EPSILON);
            assertEquals(0, waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(2);
            assertEquals(15.7, waypoint.getX(), EPSILON);
            assertEquals(-0.16, waypoint.getY(), EPSILON);
            assertEquals(0.35, waypoint.getTangentX(), EPSILON);
            assertEquals(-2.56,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(3);
            assertEquals(0.25, waypoint.getX(), EPSILON);
            assertEquals(-0.11, waypoint.getY(), EPSILON);
            assertEquals(1.3, waypoint.getTangentX(), EPSILON);
            assertEquals(-5.2,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(4);
            assertEquals(15.9, waypoint.getX(), EPSILON);
            assertEquals(-8.11, waypoint.getY(), EPSILON);
            assertEquals(-4.10, waypoint.getTangentX(), EPSILON);
            assertEquals(0.69,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(5);
            assertEquals(2.9, waypoint.getX(), EPSILON);
            assertEquals(-4, waypoint.getY(), EPSILON);
            assertEquals(-1.8, waypoint.getTangentX(), EPSILON);
            assertEquals(0.0,waypoint.getTangentY(), EPSILON);
        }

        // Make sure the path matches the one loaded from the expected file
        extractResource("/edu/wpi/first/pathweaver/" + expectedFileName, Paths.get(pathDirectory, expectedFileName));

        Path expectedPath = PathIOUtil.importPath(pathDirectory, expectedFileName);
        assertEquals(expectedPath.getWaypoints(), path.getWaypoints(), "Path loaded from disk doesn't match original");

    }


    @Test
    public void loadLegacyFileInches() throws IOException {
        String legacyFileName = "InchesLegacyPath.csv";
        String expectedFileName = "InchesExpectedPath.json";
        String pathweaverFile = "InchesPathweaverConfig.json";
        setupConfigFiles(legacyFileName, pathweaverFile);
        ProjectPreferences.getInstance(projectDirectory);

        // We expect the legacy path to exist
        assertTrue(Files.exists(Paths.get(pathDirectory, legacyFileName)));

        Path path = PathIOUtil.importPath(pathDirectory, legacyFileName);
        assertNotNull(path);
        assertEquals(7, path.getWaypoints().size());

        // After loading, the legacy version should be deleted
        assertFalse(Files.exists(Paths.get(pathDirectory, legacyFileName)));


        // Check that the points match
        {
            Waypoint waypoint = path.getWaypoints().get(0);
            assertEquals(4.7, waypoint.getX(), EPSILON);
            assertEquals(-178, waypoint.getY(), EPSILON);
            assertEquals(13.02, waypoint.getTangentX(), EPSILON);
            assertEquals(-1.14, waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(1);
            assertEquals(180.5, waypoint.getX(), EPSILON);
            assertEquals(-84.5, waypoint.getY(), EPSILON);
            assertEquals(115.70, waypoint.getTangentX(), EPSILON);
            assertEquals(1.32, waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(2);
            assertEquals(351.5, waypoint.getX(), EPSILON);
            assertEquals(-163.5, waypoint.getY(), EPSILON);
            assertEquals(7.27, waypoint.getTangentX(), EPSILON);
            assertEquals(65.02,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(3);
            assertEquals(317.0, waypoint.getX(), EPSILON);
            assertEquals(-41.0, waypoint.getY(), EPSILON);
            assertEquals(-64.72, waypoint.getTangentX(), EPSILON);
            assertEquals(103.15,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(4);
            assertEquals(49, waypoint.getX(), EPSILON);
            assertEquals(-7.0, waypoint.getY(), EPSILON);
            assertEquals(-36.09, waypoint.getTangentX(), EPSILON);
            assertEquals(-15.08,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(5);
            assertEquals(22.5, waypoint.getX(), EPSILON);
            assertEquals(-45.5, waypoint.getY(), EPSILON);
            assertEquals(-4.13, waypoint.getTangentX(), EPSILON);
            assertEquals(-30.43,waypoint.getTangentY(), EPSILON);
        }

        {
            Waypoint waypoint = path.getWaypoints().get(6);
            assertEquals(57.5, waypoint.getX(), EPSILON);
            assertEquals(-97.5, waypoint.getY(), EPSILON);
            assertEquals(43.62, waypoint.getTangentX(), EPSILON);
            assertEquals(-3.42,waypoint.getTangentY(), EPSILON);
        }

        // Make sure the path matches the one loaded from the expected file
        extractResource("/edu/wpi/first/pathweaver/" + expectedFileName, Paths.get(pathDirectory, expectedFileName));

        Path expectedPath = PathIOUtil.importPath(pathDirectory, expectedFileName);
        assertEquals(expectedPath.getWaypoints(), path.getWaypoints(), "Path loaded from disk doesn't match original");

    }


    private void extractResource(String resourceUrl, java.nio.file.Path destination) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(resourceUrl)) {
            if (stream == null) {
                fail();
            } else {
                Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void setupConfigFiles(String pathFile, String pathweaverFile) throws IOException {
        extractResource("/edu/wpi/first/pathweaver/" + pathFile, Paths.get(pathDirectory, pathFile));
        extractResource("/edu/wpi/first/pathweaver/" + pathweaverFile, Paths.get(projectDirectory, "pathweaver.json"));
    }
}
