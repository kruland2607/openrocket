package net.sf.openrocket.library;

public class LibraryEntry {

	private RemoteHost host;
	private String name;
	private String author;
	private String category;
	private String downloadURL;
	private String webURL;
	
	public RemoteHost getHost() {
		return host;
	}
	public void setHost(RemoteHost host) {
		this.host = host;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDownloadURL() {
		return downloadURL;
	}
	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}
	public String getWebURL() {
		return webURL;
	}
	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}

}
