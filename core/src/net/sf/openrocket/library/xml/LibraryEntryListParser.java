package net.sf.openrocket.library.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.library.LibraryEntry;
import net.sf.openrocket.library.RemoteHost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LibraryEntryListParser {
	
	private static final Logger log = LoggerFactory.getLogger(LibraryEntryListParser.class);
	
	private WarningSet warnings = new WarningSet();
	
	private List<LibraryEntry> entries = new ArrayList<LibraryEntry>();
	
	private RemoteHost host;
	
	public LibraryEntryListParser() {
		super();
		this.host = null;
	}
	
	public LibraryEntryListParser(RemoteHost host) {
		super();
		this.host = host;
	}
	
	public List<LibraryEntry> parse(InputStream is) throws IOException {
		
		InputSource xmlSource = new InputSource(is);
		
		return parse(xmlSource);
		
	}
	
	public List<LibraryEntry> parse(InputSource source) throws IOException {
		
		LibraryEntryListHandler handler = new LibraryEntryListHandler();
		
		try {
			SimpleSAX.readXML(source, handler, warnings);
		} catch (SAXException e) {
			log.warn("Malformed XML in input");
			throw new LibraryEntryParseException("Malformed XML in input.", e);
		}
		
		return entries;
		
	}
	
	private class LibraryEntryListHandler extends AbstractElementHandler {
		
		private LibraryEntry currentEntry = null;
		
		@Override
		public ElementHandler openElement(String element,
				HashMap<String, String> attributes, WarningSet warnings)
				throws SAXException {
			if ("entry".equals(element)) {
				currentEntry = new LibraryEntry();
				currentEntry.setHost(LibraryEntryListParser.this.host);
			}
			return this;
		}
		
		@Override
		public void closeElement(String element,
				HashMap<String, String> attributes, String content,
				WarningSet warnings) throws SAXException {
			
			if ("entry".equals(element)) {
				LibraryEntryListParser.this.entries.add(currentEntry);
			} else if ("name".equals(element)) {
				currentEntry.setName(content);
			} else if ("author".equals(element)) {
				currentEntry.setAuthor(content);
			} else if ("category".equals(element)) {
				currentEntry.setCategory(content);
			} else if ("downloadurl".equals(element)) {
				currentEntry.setDownloadURL(content);
			} else if ("weburl".equals(element)) {
				currentEntry.setWebURL(content);
			}
			
		}
		
	}
	
}
