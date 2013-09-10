package net.sf.openrocket.gui.dialogs.library;

import java.util.List;

import javax.print.attribute.UnmodifiableSetException;
import javax.swing.DefaultComboBoxModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.library.RemoteHost;
import net.sf.openrocket.library.RemoteHostRegistry;
import net.sf.openrocket.startup.Application;


public class RemoteHostComboModel extends DefaultComboBoxModel<String> {

	private static final Translator trans = Application.getTranslator();

	private static String[] hosts;
	
	static {
		List<RemoteHost> hostsList = RemoteHostRegistry.getHosts();
		hosts = new String[hostsList.size() + 1];
		hosts[0] = trans.get("LoadFromLibraryDialog.select");
		int i = 1;
		for( RemoteHost host : hostsList ) {
			
			hosts[i++] = host.getDisplayName();
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
		
		return null;
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
