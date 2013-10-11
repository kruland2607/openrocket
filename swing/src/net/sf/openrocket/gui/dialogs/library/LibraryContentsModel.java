package net.sf.openrocket.gui.dialogs.library;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.library.LibraryEntry;
import net.sf.openrocket.startup.Application;

public class LibraryContentsModel extends AbstractTableModel {

	private static final Translator trans = Application.getTranslator();

	private List<LibraryEntry> contents = Collections.<LibraryEntry>emptyList();

	public void clear() {
		setContents( Collections.<LibraryEntry>emptyList() );
	}
	
	public void setContents( List<LibraryEntry> entries ) {
		contents = entries;
		fireTableDataChanged();
	}

	public LibraryEntry getEntry( int rowIndex ) {
		return contents.get(rowIndex);
	}
	
	@Override
	public int getRowCount() {
		return contents.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch ( columnIndex ) {
		case 0:
			return contents.get(rowIndex).getCategory();
		case 1:
			return contents.get(rowIndex).getName();
		case 2:
			return contents.get(rowIndex).getAuthor();
		default:
			return "";
		}
	}

	@Override
	public String getColumnName(int column) {
		switch( column ) {
		case 0:
			return trans.get("LoadFromLibraryDialog.table.columnLabel.Category");
		case 1:
			return trans.get("LoadFromLibraryDialog.table.columnLabel.Name");
		case 2:
			return trans.get("LoadFromLibraryDialog.table.columnLabel.Author");

		default:
			return "";
		}
	}


}
