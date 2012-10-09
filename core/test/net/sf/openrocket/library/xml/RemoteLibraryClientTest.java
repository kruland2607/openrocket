package net.sf.openrocket.library.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import net.sf.openrocket.communication.Communicator;
import net.sf.openrocket.communication.ConnectionSourceStub;
import net.sf.openrocket.communication.HttpURLConnectionMock;
import net.sf.openrocket.library.LibraryEntry;
import net.sf.openrocket.library.RemoteHost;
import net.sf.openrocket.library.RemoteLibraryClient;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class RemoteLibraryClientTest extends BaseTestCase {

	/** The connection delay */
	private static final int DELAY = 100;

	private RemoteHost testHost = new RemoteHost("a host", "http://127.0.0.1");
	
	private HttpURLConnectionMock setup() {
		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		Communicator.setConnectionSource(new ConnectionSourceStub(connection));
		
		connection.setConnectionDelay(DELAY);
		connection.setUseCaches(true);
		connection.setContentType("text/plain");
		return connection;
	}
	
	private void check(HttpURLConnectionMock connection) {
		assertEquals(testHost.getUrl() + "/List",
				connection.getTrueUrl());
		assertTrue(connection.getConnectTimeout() > 0);
		assertTrue(connection.getInstanceFollowRedirects());
		assertEquals("GET", connection.getRequestMethod());
		assertTrue(connection.getUseCaches());
	}
	

	@Test
	public void testHappyCase() throws Exception {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(HttpURLConnection.HTTP_OK);
		
		InputStream is = RemoteLibraryClientTest.class.getResourceAsStream("samplelist.xml");
		
		String content;
		{
			StringBuilder contentBuilder = new StringBuilder();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while( (bytesRead = is.read(buffer)) > 0 ) {
				contentBuilder.append(new String(buffer,0,bytesRead));
			}
			content = contentBuilder.toString();
		}
		connection.setContent(content);
		
		RemoteLibraryClient client = new RemoteLibraryClient(testHost);

		List<LibraryEntry> entries = client.getListing();
		
		check(connection);

		assertEquals(2, entries.size());

		{
			LibraryEntry e1 = entries.get(0);
			assertEquals(testHost,e1.getHost());
			assertEquals("name1",e1.getName());
			assertEquals("author1",e1.getAuthor());
			assertEquals("category1",e1.getCategory());
			assertEquals("downloadurl1",e1.getDownloadURL());
			assertEquals("weburl1",e1.getWebURL());
		}
		{
			LibraryEntry e2 = entries.get(1);
			assertEquals(testHost,e2.getHost());
			assertEquals("name2",e2.getName());
			assertEquals("author2",e2.getAuthor());
			assertEquals("category2",e2.getCategory());
			assertEquals("downloadurl2",e2.getDownloadURL());
			assertEquals("weburl2",e2.getWebURL());
		}

	}

}
