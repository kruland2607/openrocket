package net.sf.openrocket.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import net.sf.openrocket.gui.dialogs.ExampleDesignDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

public class FileMenuFactory {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	static JMenu makeFileMenu( final BasicFrame parent ) {
		JMenu menu;
		JMenuItem item;

		////  File
		menu = new JMenu(trans.get("main.menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		//// File-handling related tasks
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));

		//// New
		item = new JMenuItem(trans.get("main.menu.file.new"), KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_N);
		//// Create a new rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.new.desc"));
		item.setIcon(Icons.FILE_NEW);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("New... selected");
				BasicFrame.newAction();
				parent.closeIfReplaceable();
			}
		});
		menu.add(item);

		//// Open...
		item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		//// Open a rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Openrocketdesign"));
		item.setIcon(Icons.FILE_OPEN);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Open... selected");
				parent.openAction();
			}
		});
		menu.add(item);

		//// Open Recent...
		item = new MRUDesignFileAction(trans.get("main.menu.file.openRecent"), parent.getRootWindow());
		//// Open a recent rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Openrecentrocketdesign"));
		item.setIcon(Icons.FILE_OPEN);
		menu.add(item);

		//// Open example...
		item = new JMenuItem(trans.get("main.menu.file.openExample"));
		//// Open an example rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Openexamplerocketdesign"));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		item.setIcon(Icons.FILE_OPEN_EXAMPLE);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Open example... selected");
				URL[] urls = ExampleDesignDialog.selectExampleDesigns(parent.getRootWindow());
				if (urls != null) {
					for (URL u : urls) {
						log.user("Opening example " + u);
						DocumentManager.open(u, parent.getRootWindow());
					}
				}
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Save
		item = new JMenuItem(trans.get("main.menu.file.save"), KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		//// Save the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.SavecurRocketdesign"));
		item.setIcon(Icons.FILE_SAVE);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Save selected");
				parent.saveAction();
			}
		});
		menu.add(item);

		//// Save as...
		item = new JMenuItem(trans.get("main.menu.file.saveAs"), KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		//// Save the current rocket design to a new file
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.SavecurRocketdesnewfile"));
		item.setIcon(Icons.FILE_SAVE_AS);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Save as... selected");
				parent.saveAsAction();
			}
		});
		menu.add(item);

		//// Print...
		item = new JMenuItem(trans.get("main.menu.file.print"), KeyEvent.VK_P);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		//// Print parts list and fin template
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.print.desc"));
		item.setIcon(Icons.FILE_PRINT);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Print action selected");
				parent.printAction();
			}
		});
		menu.add(item);


		menu.addSeparator();

		//// Close
		item = new JMenuItem(trans.get("main.menu.file.close"), KeyEvent.VK_C);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		//// Close the current rocket design
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Closedesign"));
		item.setIcon(Icons.FILE_CLOSE);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Close selected");
				parent.closeAction();
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Quit
		item = new JMenuItem(trans.get("main.menu.file.quit"), KeyEvent.VK_Q);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		//// Quit the program
		item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Quitprogram"));
		item.setIcon(Icons.FILE_QUIT);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Quit selected");
				DocumentManager.quitAction();
			}
		});
		menu.add(item);

		return menu;
	}
}
