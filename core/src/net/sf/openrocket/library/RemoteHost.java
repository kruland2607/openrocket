package net.sf.openrocket.library;

public class RemoteHost {

	private String displayName;
	private String url;
	
	public RemoteHost() {
		this.displayName = "";
		this.url = "";
	}
	
	public RemoteHost(String displayName, String url) {
		this.displayName = displayName;
		this.url = url;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
