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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}


}
