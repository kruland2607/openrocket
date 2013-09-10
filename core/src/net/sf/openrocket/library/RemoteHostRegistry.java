package net.sf.openrocket.library;

import java.util.ArrayList;
import java.util.List;

public class RemoteHostRegistry {

	private static List<RemoteHost> hosts = new ArrayList<RemoteHost>();

	static {
		hosts.add( new RemoteHost("OpenRocket Examples", "https://raw.github.com/kruland2607/openrocket/library-featuredownload/core/resources/datafiles/examples/") );
	}
	
	public static List<RemoteHost> getHosts() {
		return hosts;
	}

}
