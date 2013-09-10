package net.sf.openrocket.library;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import net.sf.openrocket.communication.Communicator;
import net.sf.openrocket.library.xml.LibraryEntryListParser;
import net.sf.openrocket.util.BugException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteLibraryClient {
	
	private static final Logger log = LoggerFactory.getLogger(RemoteLibraryClient.class);
	
	private RemoteHost host;
	
	public RemoteLibraryClient(RemoteHost host) {
		this.host = host;
	}
	
	public List<LibraryEntry> getListing() throws RemoteLibraryException {
		
		HttpURLConnection connection;
		try {
			connection = Communicator.getConnection(host.getUrl() + "/List");
		} catch (IOException ex) {
			throw new RemoteLibraryException(ex);
		}
		
		connection.setConnectTimeout(10000);
		connection.setInstanceFollowRedirects(true);
		connection.setUseCaches(true);
		
		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException ex) {
			throw new BugException(ex);
		}
		connection.setDoInput(true);
		
		try {
			connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// Some problem communicating ?
				log.info("No OK recieved.  Status code = " + connection.getResponseCode());
				throw new RemoteLibraryException("Invalid status code");
			}
			
			InputStream is = connection.getInputStream();
			
			LibraryEntryListParser parser = new LibraryEntryListParser(host);
			List<LibraryEntry> entries = parser.parse(is);
			
			return entries;
			
		} catch (IOException ex) {
			throw new RemoteLibraryException(ex);
		}
		
	}
	
	public InputStream downloadModel(String downloadURL) throws RemoteLibraryException {
		
		HttpURLConnection connection;
		try {
			URI hostURI = new URI(host.getUrl());
			URI fileURI = new URI(hostURI.getScheme(), null, hostURI.getHost(), hostURI.getPort(), hostURI.getPath() + "/" + downloadURL, null, null);
			String fileURL = fileURI.toString();
			log.debug("Loading model file: " + fileURL);
			connection = Communicator.getConnection(fileURL);
		} catch (IOException ex) {
			throw new RemoteLibraryException(ex);
		} catch (URISyntaxException ex) {
			throw new RemoteLibraryException(ex);
		}
		
		connection.setConnectTimeout(10000);
		connection.setInstanceFollowRedirects(true);
		connection.setUseCaches(false);
		
		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException ex) {
			throw new BugException(ex);
		}
		connection.setDoInput(true);
		
		try {
			connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// Some problem communicating ?
				log.info("No OK recieved.  Status code = " + connection.getResponseCode());
				throw new RemoteLibraryException("Invalid status code");
			}
			
			InputStream is = connection.getInputStream();
			
			return is;
			
		} catch (IOException ex) {
			throw new RemoteLibraryException(ex);
		}
		
	}
	
}
