package net.sf.openrocket.library;

public class RemoteLibraryException extends Exception {

	public RemoteLibraryException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteLibraryException(String message) {
		super(message);
	}

	public RemoteLibraryException(Throwable cause) {
		super(cause);
	}

}
