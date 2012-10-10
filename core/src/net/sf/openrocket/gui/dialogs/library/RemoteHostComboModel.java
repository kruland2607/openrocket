package net.sf.openrocket.gui.dialogs.library;

import java.util.List;

import javax.print.attribute.UnmodifiableSetException;
import javax.swing.DefaultComboBoxModel;

import net.sf.openrocket.library.RemoteHost;
import net.sf.openrocket.library.RemoteHostRegistry;


public class RemoteHostComboModel extends DefaultComboBoxModel<String> {

	private static String[] hosts;
	
	static {
		List<RemoteHost> hostsList = RemoteHostRegistry.getHosts();
		hosts = new String[hostsList.size()];
		for( int i =0; i< hostsList.size(); i++ ) {
			
			hosts[i] = hostsList.get(i).getDisplayName();
		}
	}
	
	public RemoteHostComboModel() {
		super( hosts );
	}
	
	public RemoteHost getSelectedRemoteHost() {
		String host = (String) super.getSelectedItem();
		
		for( RemoteHost h: RemoteHostRegistry.getHosts() ) {
			if ( h.getDisplayName().equals(host) ) {
				return h;
			}
		}
		
		return new RemoteHost( host, host);
	}
	
	@Override
	public void addElement(String anObject) {
		throw new UnmodifiableSetException();
	}

	@Override
	public void insertElementAt(String anObject, int index) {
		throw new UnmodifiableSetException();
	}

	@Override
	public void removeElementAt(int index) {
		throw new UnmodifiableSetException();
	}

	@Override
	public void removeElement(Object anObject) {
		throw new UnmodifiableSetException();
	}

	@Override
	public void removeAllElements() {
		// TODO Auto-generated method stub
		super.removeAllElements();
	}

	
	
}
