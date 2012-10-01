package net.sf.openrocket.gui.main;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.BugReportDialog;
import net.sf.openrocket.gui.dialogs.DebugLogDialog;
import net.sf.openrocket.gui.dialogs.LicenseDialog;
import net.sf.openrocket.gui.help.tours.GuidedTourSelectionDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

public class HelpMenuFactory {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	static JMenu makeHelpMenu( final BasicFrame parent ) {
		JMenu menu;
		JMenuItem item;

		menu = new JMenu(trans.get("main.menu.help"));
		menu.setMnemonic(KeyEvent.VK_H);
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.desc"));


		// Guided tours

		item = new JMenuItem(trans.get("main.menu.help.tours"), KeyEvent.VK_L);
		item.setIcon(Icons.HELP_TOURS);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.tours.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Guided tours selected");
				GuidedTourSelectionDialog.showDialog(parent.getRootWindow());
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Bug report
		item = new JMenuItem(trans.get("main.menu.help.bugReport"), KeyEvent.VK_B);
		item.setIcon(Icons.HELP_BUG_REPORT);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.bugReport.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Bug report selected");
				BugReportDialog.showBugReportDialog(parent.getRootWindow());
			}
		});
		menu.add(item);

		//// Debug log
		item = new JMenuItem(trans.get("main.menu.help.debugLog"));
		item.setIcon(Icons.HELP_DEBUG_LOG);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.debugLog.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Debug log selected");
				new DebugLogDialog(parent.getRootWindow()).setVisible(true);
			}
		});
		menu.add(item);

		menu.addSeparator();


		//// License
		item = new JMenuItem(trans.get("main.menu.help.license"), KeyEvent.VK_L);
		item.setIcon(Icons.HELP_LICENSE);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.license.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("License selected");
				new LicenseDialog(parent.getRootWindow()).setVisible(true);
			}
		});
		menu.add(item);


		//// About
		item = new JMenuItem(trans.get("main.menu.help.about"), KeyEvent.VK_A);
		item.setIcon(Icons.HELP_ABOUT);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.about.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("About selected");
				new AboutDialog(parent.getRootWindow()).setVisible(true);
			}
		});
		menu.add(item);

		return menu;
	}
}
