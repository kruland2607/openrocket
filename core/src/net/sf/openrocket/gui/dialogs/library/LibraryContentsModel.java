package net.sf.openrocket.gui.dialogs.library;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.library.LibraryEntry;

public class LibraryContentsModel extends AbstractTableModel {

	List<LibraryEntry> contents;
	
	public void setContents( List<LibraryEntry> entries ) {
		
	}
	
	@Override
	public int getRowCount() {
		return contents.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return contents.get(rowIndex).getDownloadURL();
	}


}
