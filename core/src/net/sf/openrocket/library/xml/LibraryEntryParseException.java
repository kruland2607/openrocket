package net.sf.openrocket.library.xml;

import java.io.IOException;

public class LibraryEntryParseException extends IOException {

	public LibraryEntryParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public LibraryEntryParseException(String message) {
		super(message);
	}

}
