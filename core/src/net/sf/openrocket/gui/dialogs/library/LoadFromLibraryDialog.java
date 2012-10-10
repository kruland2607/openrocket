package net.sf.openrocket.gui.dialogs.library;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.library.LibraryEntry;
import net.sf.openrocket.library.RemoteHost;
import net.sf.openrocket.library.RemoteLibraryClient;
import net.sf.openrocket.library.RemoteLibraryException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

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
		hosts.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				RemoteHostComboModel model = (RemoteHostComboModel) combo.getModel();
				RemoteHost host = model.getSelectedRemoteHost();
				LoadFromLibraryDialog.this.loadDataFromRemoteHost(host);
			}

		});
		sub.add(hosts, "growx");

		panel.add(sub, "grow, ay 0,wrap para");

		final JTable modelSelectionTable = new JTable(libraryContentsModel);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(libraryContentsModel);
		modelSelectionTable.setRowSorter(sorter);
		modelSelectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(modelSelectionTable);
		panel.add(scrollpane, "grow, width 700lp, height 300lp, spanx, wrap rel");

		// OK / Cancel buttons
		JButton okButton = new JButton(trans.get("dlg.but.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = modelSelectionTable.getSelectedRow();
				selectedRow = modelSelectionTable.convertRowIndexToModel(selectedRow);
				LibraryEntry entry = LoadFromLibraryDialog.this.libraryContentsModel.getEntry(selectedRow);
				loadSelection(entry);
			}
		});
		panel.add(okButton, "tag ok, spanx, split");

		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		panel.add(cancelButton, "tag cancel");

		this.add(panel);

	}

	private void loadDataFromRemoteHost( RemoteHost host ) {

		try {
			RemoteLibraryClient client = new RemoteLibraryClient( host );
			List<LibraryEntry> entries = client.getListing();
			libraryContentsModel.setContents(entries);
			libraryContentsModel.fireTableDataChanged();
		} catch (RemoteLibraryException rex ) {
			// FIXME - display dialog
			throw new BugException(rex);
		}
	}

	private void loadSelection( LibraryEntry entry ) {

		try {
			RemoteLibraryClient client = new RemoteLibraryClient( entry.getHost() );
			InputStream is = client.downloadModel(entry.getDownloadURL());
			BasicFrame.open(is, entry.getName(), this.getOwner() );
		} catch ( RemoteLibraryException rex ) {
			// FIXME - display dialog
			throw new BugException(rex);
		} finally {
			close();
		}
	}

	private void close() {
		this.setVisible(false);
	}
}
