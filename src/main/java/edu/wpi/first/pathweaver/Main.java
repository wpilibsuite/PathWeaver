package edu.wpi.first.pathweaver;

import javafx.application.Application;

/**
 * The true main class. This bypasses module boot layer introspection by the
 * Java launcher that attempts to reflectively access the JavaFX application
 * launcher classes - this will fail because there is no module path; everything
 * is in the same, unnamed module.
 */
// Nope.
@SuppressWarnings("PMD.UseUtilityClass")
public final class Main {
	public static void main(String[] args) {
		// JavaFX 11+ uses GTK3 by default, and has problems on some display servers
		// This flag forces JavaFX to use GTK2
		System.setProperty("jdk.gtk.version", "2");
		Application.launch(PathWeaver.class, args);
	}
}
