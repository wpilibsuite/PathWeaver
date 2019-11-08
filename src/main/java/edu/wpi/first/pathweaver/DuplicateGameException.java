package edu.wpi.first.pathweaver;

public class DuplicateGameException extends RuntimeException {
	public DuplicateGameException(String message) {
		super(message);
	}

	public DuplicateGameException(String message, Throwable cause) {
		super(message, cause);
	}
}
