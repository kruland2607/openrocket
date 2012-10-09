package net.sf.openrocket.library.xml;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.library.LibraryEntry;

import org.junit.Test;

public class LibraryEntryListParserTest {

	@Test
	public void testWithoutRemoteHost() throws Exception {

		InputStream is = LibraryEntryListParserTest.class.getResourceAsStream("samplelist.xml");

		LibraryEntryListParser parser = new LibraryEntryListParser();
		List<LibraryEntry> entries = parser.parse(is);

		assertEquals(2, entries.size());

		{
			LibraryEntry e1 = entries.get(0);
			assertNull(e1.getHost());
			assertEquals("name1",e1.getName());
			assertEquals("author1",e1.getAuthor());
			assertEquals("category1",e1.getCategory());
			assertEquals("downloadurl1",e1.getDownloadURL());
			assertEquals("weburl1",e1.getWebURL());
		}
		{
			LibraryEntry e2 = entries.get(1);
			assertNull(e2.getHost());
			assertEquals("name2",e2.getName());
			assertEquals("author2",e2.getAuthor());
			assertEquals("category2",e2.getCategory());
			assertEquals("downloadurl2",e2.getDownloadURL());
			assertEquals("weburl2",e2.getWebURL());
		}
	}
}
