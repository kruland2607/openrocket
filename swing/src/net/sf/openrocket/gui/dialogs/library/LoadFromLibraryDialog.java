package net.sf.openrocket.gui.dialogs.library;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.library.LibraryEntry;
import net.sf.openrocket.library.RemoteHost;
import net.sf.openrocket.library.RemoteLibraryClient;
import net.sf.openrocket.library.RemoteLibraryException;
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
		
		JComboBox hosts = new JComboBox(new RemoteHostComboModel());
		hosts.setEditable(true);
		hosts.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				RemoteHostComboModel model = (RemoteHostComboModel) combo.getModel();
				RemoteHost host = model.getSelectedRemoteHost();
				if (host == null) {
					LoadFromLibraryDialog.this.clear();
				} else {
					LoadFromLibraryDialog.this.loadDataFromRemoteHost(host);
				}
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
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, cancelButton);
		
	}
	
	private void clear() {
		libraryContentsModel.clear();
	}
	
	private void loadDataFromRemoteHost(RemoteHost host) {
		
		try {
			RemoteLibraryClient client = new RemoteLibraryClient(host);
			List<LibraryEntry> entries = client.getListing();
			libraryContentsModel.setContents(entries);
		} catch (RemoteLibraryException rex) {
			libraryContentsModel.clear();
			showErrorDialog(rex.getLocalizedMessage());
		}
	}
	
	private void loadSelection(LibraryEntry entry) {
		
		try {
			RemoteLibraryClient client = new RemoteLibraryClient(entry.getHost());
			InputStream is = client.downloadModel(entry.getDownloadURL());
			
			File tmpFile = File.createTempFile("OpenRocketLibrary", ".ork");
			{
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(tmpFile));
				InputStream bis = new BufferedInputStream(is);
				byte[] buffer = new byte[2048];
				int bytesRead = 0;
				while ((bytesRead = bis.read(buffer)) > 0) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.close();
				bis.close();
			}
			BasicFrame.open(tmpFile, this.getOwner());
		} catch (RemoteLibraryException rex) {
			showErrorDialog(rex.getLocalizedMessage());
		} catch (IOException ioex) {
			showErrorDialog(ioex.getLocalizedMessage());
		} finally {
			close();
		}
	}
	
	private void close() {
		this.setVisible(false);
	}
	
	private void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(this, trans.get("LoadFromLibraryDialog.error.message") + "\n" + message, trans.get("LoadFromLibraryDialog.error.title"), JOptionPane.ERROR_MESSAGE);
	}
}
