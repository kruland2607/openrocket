package net.sf.openrocket.gui.dialogs.library;

import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.library.RemoteHostRegistry;
import net.sf.openrocket.startup.Application;

public class LoadFromLibraryDialog extends JDialog {

	private static final Translator trans = Application.getTranslator();
	
	private LibraryContentsModel libraryContentsModel = new LibraryContentsModel();

	public LoadFromLibraryDialog(Window owner) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);

	
		JPanel panel = new JPanel(new MigLayout("fill, ins para"));

		//// Hosts drop down.
		JPanel sub = new JPanel(new MigLayout("fill, ins 0"));
		JLabel filterLabel = new JLabel(trans.get("host.label"));
		sub.add(filterLabel, "gapright para");
		
		JComboBox<String> hosts = new JComboBox<String>(new RemoteHostComboModel() );
		hosts.setEditable(true);
		sub.add(hosts, "growx");

		panel.add(sub, "growx, ay 0,wrap para");

		JTable modelSelectionTable = new JTable(libraryContentsModel);
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(modelSelectionTable);
		panel.add(scrollpane, "grow, width 700lp, height 300lp, spanx, wrap rel");

		this.add(panel);

	}


}
