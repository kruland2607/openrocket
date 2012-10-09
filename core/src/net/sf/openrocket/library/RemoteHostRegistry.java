package net.sf.openrocket.library;

import java.util.ArrayList;
import java.util.List;

public class RemoteHostRegistry {

	private static List<RemoteHost> hosts = new ArrayList<RemoteHost>();

	static {
		hosts.add( new RemoteHost("localhost", "http://127.0.0.1/library") );
	}
	
	public static List<RemoteHost> getHosts() {
		return hosts;
	}

}
