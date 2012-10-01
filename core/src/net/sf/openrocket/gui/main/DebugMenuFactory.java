package net.sf.openrocket.gui.main;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.DetailDialog;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MemoryManagement;
import net.sf.openrocket.util.MemoryManagement.MemoryData;
import net.sf.openrocket.util.TestRockets;

public class DebugMenuFactory {
	private static final LogHelper log = Application.getLogger();

	static JMenu makeDebugMenu( final Window parent ) {
		JMenu menu;
		JMenuItem item;

		/*
		 * This menu is intentionally left untranslated.
		 */

		////  Debug menu
		menu = new JMenu("Debug");
		//// OpenRocket debugging tasks
		menu.getAccessibleContext().setAccessibleDescription("OpenRocket debugging tasks");

		//// What is this menu?
		item = new JMenuItem("What is this menu?");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("What is this menu? selected");
				JOptionPane.showMessageDialog(parent,
						new Object[] {
								"The 'Debug' menu includes actions for testing and debugging " +
										"OpenRocket.", " ",
								"The menu is made visible by defining the system property " +
										"'openrocket.debug.menu' when starting OpenRocket.",
								"It should not be visible by default." },
						"Debug menu", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(item);

		menu.addSeparator();

		//// Create test rocket
		item = new JMenuItem("Create test rocket");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Create test rocket selected");
				JTextField field = new JTextField();
				int sel = JOptionPane.showOptionDialog(parent, new Object[] {
						"Input text key to generate random rocket:",
						field
				}, "Generate random test rocket", JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, new Object[] {
								"Random", "OK"
						}, "OK");

				Rocket r;
				if (sel == 0) {
					r = new TestRockets(null).makeTestRocket();
				} else if (sel == 1) {
					r = new TestRockets(field.getText()).makeTestRocket();
				} else {
					return;
				}

				OpenRocketDocument doc = new OpenRocketDocument(r);
				doc.setSaved(true);
				BasicFrame frame = BasicFrame.Builder.newInstance(doc);
			}
		});
		menu.add(item);



		item = new JMenuItem("Create 'Iso-Haisu'");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Create Iso-Haisu selected");
				Rocket r = TestRockets.makeIsoHaisu();
				OpenRocketDocument doc = new OpenRocketDocument(r);
				doc.setSaved(true);
				BasicFrame frame = BasicFrame.Builder.newInstance(doc);
			}
		});
		menu.add(item);


		item = new JMenuItem("Create 'Big Blue'");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Create Big Blue selected");
				Rocket r = TestRockets.makeBigBlue();
				OpenRocketDocument doc = new OpenRocketDocument(r);
				doc.setSaved(true);
				BasicFrame frame = BasicFrame.Builder.newInstance(doc);
			}
		});
		menu.add(item);

		menu.addSeparator();


		item = new JMenuItem("Memory statistics");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Memory statistics selected");

				// Get discarded but remaining objects (this also runs System.gc multiple times)
				List<MemoryData> objects = MemoryManagement.getRemainingCollectableObjects();
				StringBuilder sb = new StringBuilder();
				sb.append("Objects that should have been garbage-collected but have not been:\n");
				int count = 0;
				for (MemoryData data : objects) {
					Object o = data.getReference().get();
					if (o == null)
						continue;
					sb.append("Age ").append(System.currentTimeMillis() - data.getRegistrationTime())
							.append(" ms:  ").append(o).append('\n');
					count++;
					// Explicitly null the strong reference to avoid possibility of invisible references
					o = null;
				}
				sb.append("Total: " + count);

				// Get basic memory stats
				System.gc();
				long max = Runtime.getRuntime().maxMemory();
				long free = Runtime.getRuntime().freeMemory();
				long used = max - free;
				String[] stats = new String[4];
				stats[0] = "Memory usage:";
				stats[1] = String.format("   Max memory:  %.1f MB", max / 1024.0 / 1024.0);
				stats[2] = String.format("   Used memory: %.1f MB (%.0f%%)", used / 1024.0 / 1024.0, 100.0 * used / max);
				stats[3] = String.format("   Free memory: %.1f MB (%.0f%%)", free / 1024.0 / 1024.0, 100.0 * free / max);


				DetailDialog.showDetailedMessageDialog(parent, stats, sb.toString(),
						"Memory statistics", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(item);

		//// Exhaust memory
		item = new JMenuItem("Exhaust memory");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Exhaust memory selected");
				LinkedList<byte[]> data = new LinkedList<byte[]>();
				int count = 0;
				final int bytesPerArray = 10240;
				try {
					while (true) {
						byte[] array = new byte[bytesPerArray];
						for (int i = 0; i < bytesPerArray; i++) {
							array[i] = (byte) i;
						}
						data.add(array);
						count++;
					}
				} catch (OutOfMemoryError error) {
					data = null;
					long size = bytesPerArray * (long) count;
					String s = String.format("OutOfMemory occurred after %d iterations (approx. %.1f MB consumed)",
							count, size / 1024.0 / 1024.0);
					log.debug(s, error);
					JOptionPane.showMessageDialog(parent, s);
				}
			}
		});
		menu.add(item);


		menu.addSeparator();

		//// Exception here
		item = new JMenuItem("Exception here");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Exception here selected");
				throw new RuntimeException("Testing exception from menu action listener");
			}
		});
		menu.add(item);

		item = new JMenuItem("Exception from EDT");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Exception from EDT selected");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						throw new RuntimeException("Testing exception from " +
								"later invoked EDT thread");
					}
				});
			}
		});
		menu.add(item);

		item = new JMenuItem("Exception from other thread");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Exception from other thread selected");
				new Thread() {
					@Override
					public void run() {
						throw new RuntimeException("Testing exception from newly created thread");
					}
				}.start();
			}
		});
		menu.add(item);

		item = new JMenuItem("OutOfMemoryError here");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("OutOfMemoryError here selected");
				throw new OutOfMemoryError("Testing OutOfMemoryError from menu action listener");
			}
		});
		menu.add(item);


		menu.addSeparator();


		item = new JMenuItem("Test popup");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Test popup selected");
				JPanel panel = new JPanel();
				panel.add(new JTextField(40));
				panel.add(new JSpinner());
				JPopupMenu popup = new JPopupMenu();
				popup.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				popup.add(panel);
				popup.show(parent, -50, 100);
			}
		});
		menu.add(item);




		return menu;
	}

}
