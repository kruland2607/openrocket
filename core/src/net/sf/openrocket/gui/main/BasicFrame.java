package net.sf.openrocket.gui.main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.file.rocksim.export.RocksimSaver;
import net.sf.openrocket.gui.StorageOptionChooser;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.customexpression.CustomExpressionDialog;
import net.sf.openrocket.gui.dialogs.ComponentAnalysisDialog;
import net.sf.openrocket.gui.dialogs.PrintDialog;
import net.sf.openrocket.gui.dialogs.ScaleDialog;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.gui.dialogs.optimization.GeneralOptimizationDialog;
import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import net.sf.openrocket.gui.main.componenttree.ComponentTree;
import net.sf.openrocket.gui.preset.ComponentPresetEditor;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.FlowingButtonBox;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SaveFileWorker;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Reflection;

public class BasicFrame extends JFrame {

	public static final class Builder {
		public static BasicFrame newInstance( OpenRocketDocument document ) {
			BasicFrame frame = new BasicFrame(document);
			DocumentManager.addFrame(frame);
			frame.setVisible(true);
			return frame;
		}
	}

	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	private static final RocketSaver ROCKET_SAVER = new OpenRocketSaver();

	private View simulationPanelView;
	private View designTabView;
	private View componentToolboxView;
	private View rocketView;

	/**
	 * Whether "New" and "Open" should replace this frame.
	 * Should be set to false on the first rocket modification.
	 */
	private boolean replaceable = false;

	private final OpenRocketDocument document;
	private final Rocket rocket;

	OpenRocketDocument getDocument() {
		return document;
	}
	Rocket getRocket() {
		return rocket;
	}

	private final RootWindow rootWindow;
	public Window getRootWindow() {
		return this;
	}

	private RocketPanel rocketpanel;
	private ComponentTree tree = null;

	private final DocumentSelectionModel selectionModel;
	private final TreeSelectionModel componentSelectionModel;
	private final ListSelectionModel simulationSelectionModel;

	/** Actions available for rocket modifications */
	private final RocketActions actions;

	/**
	 * Sole constructor.  Creates a new frame based on the supplied document
	 * and adds it to the current frames list.
	 *
	 * @param document	the document to show.
	 */
	private BasicFrame(OpenRocketDocument document) {
		log.debug("Instantiating new BasicFrame");
		this.document = document;
		this.rocket = document.getRocket();
		this.rocket.getDefaultConfiguration().setAllStages();

		// Create the component tree selection model that will be used
		componentSelectionModel = new DefaultTreeSelectionModel();
		componentSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Obtain the simulation selection model that will be used
		SimulationPanel simulationPanel = new SimulationPanel(this, document);
		simulationSelectionModel = simulationPanel.getSimulationListSelectionModel();

		// Combine into a DocumentSelectionModel
		selectionModel = new DocumentSelectionModel(document);
		selectionModel.attachComponentTreeSelectionModel(componentSelectionModel);
		selectionModel.attachSimulationListSelectionModel(simulationSelectionModel);

		actions = new RocketActions(document, selectionModel, this);

		log.debug("Constructing the BasicFrame UI");

		simulationPanelView = new View(trans.get("BasicFrame.tab.Flightsim"), null, simulationPanel);
		designTabView = new View(trans.get("BasicFrame.tab.Rocketdesign"), null, designTab());
		componentToolboxView = new View("Toolbox",null, makeComponentToolbar());

		//  Bottom segment, rocket figure

		rocketpanel = new RocketPanel(document);
		rocketpanel.setSelectionModel(tree.getSelectionModel());

		rocketView = new View("RocketFigure", null, rocketpanel);

		rootWindow = DockingUtil.createRootWindow(new ViewMap(), true);
		rootWindow.setWindow(new SplitWindow( /*horizontal*/ true, 0.25f,
				new SplitWindow( false, 0.7f, designTabView, componentToolboxView ),
				new TabWindow( new DockingWindow[] {
						simulationPanelView,
						rocketView } ) )
				);

		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		DockingWindowsTheme theme = new ShapedGradientDockingTheme();
		rootWindow.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
		RootWindowProperties titleBarStyleProps = PropertiesUtil.createTitleBarStyleRootWindowProperties();
		rootWindow.getRootWindowProperties().addSuperObject(titleBarStyleProps);

		createMenu();


		rocket.addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				setTitle();
			}
		});

		setTitle();

		// Set initial window size
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width = size.width * 9 / 10;
		size.height = size.height * 9 / 10;
		rootWindow.setSize(size);

		// Remember changed size
		GUIUtil.rememberWindowSize(this);

		this.setLocationByPlatform(true);

		GUIUtil.setWindowIcons(this);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeAction();
			}
		});

		this.add(rootWindow);
		log.debug("BasicFrame instantiation complete");
	}

	/**
	 * Construct the "Rocket design" tab.  This contains a horizontal split pane
	 * with the left component the design tree and the right component buttons
	 * for adding components.
	 */
	private JComponent designTab() {
		//  Upper-left segment, component tree

		JViewport view = new JViewport();
		JPanel panel = new JPanel(new MigLayout("fill", "", "[] [grow]"));
		view.add(panel);
		FlowingButtonBox buttons = new FlowingButtonBox(1,5,view);
		{

			JButton[] button = new JButton[] {
					new JButton(actions.getMoveUpAction()),
					new JButton(actions.getMoveDownAction()),
					new JButton(actions.getEditAction()),
					new JButton(actions.getNewStageAction()),
					new JButton(actions.getDeleteAction())
			};
			button[4].setIcon(null);
			button[4].setMnemonic(0);
			buttons.addButtonRow(null, 0, button);

		}

		//		JPanel buttons = new JPanel(new MigLayout("flowx", ""));
		panel.add(buttons,"wrap");

		tree = new ComponentTree(document);
		tree.setSelectionModel(componentSelectionModel);

		// Remove JTree key events that interfere with menu accelerators
		InputMap im = SwingUtilities.getUIInputMap(tree, JComponent.WHEN_FOCUSED);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), null);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), null);



		// Double-click opens config dialog
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if ((e.getClickCount() == 2) && !ComponentConfigDialog.isDialogVisible()) {
						// Double-click
						RocketComponent c = (RocketComponent) selPath.getLastPathComponent();
						ComponentConfigDialog.showDialog(getRootWindow(),
								BasicFrame.this.document, c);
					}
				}
			}
		};
		tree.addMouseListener(ml);

		// Update dialog when selection is changed
		componentSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// Scroll tree to the selected item
				TreePath path = componentSelectionModel.getSelectionPath();
				if (path == null)
					return;
				tree.scrollPathToVisible(path);

				if (!ComponentConfigDialog.isDialogVisible())
					return;
				RocketComponent c = (RocketComponent) path.getLastPathComponent();
				ComponentConfigDialog.showDialog(getRootWindow(),
						BasicFrame.this.document, c);
			}
		});

		// Place tree inside scroll pane
		JScrollPane scroll = new JScrollPane(tree);
		panel.add(scroll, "spany, grow, wrap");
		buttons.postConstruct();
		view.validate();
		return view;
	}

	private JComponent makeComponentToolbar() {

		JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[0::]"));

		JScrollPane scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setViewportView(new ComponentAddButtons(document, componentSelectionModel,
				scroll.getViewport()));
		scroll.setBorder(null);
		scroll.setViewportBorder(null);

		TitledBorder border = BorderFactory.createTitledBorder(trans.get("BasicFrame.title.Addnewcomp"));
		GUIUtil.changeFontStyle(border, Font.BOLD);
		scroll.setBorder(border);

		panel.add(scroll, "grow");

		return panel;
	}

	/**
	 * Return the currently selected rocket component, or <code>null</code> if none selected.
	 */
	private RocketComponent getSelectedComponent() {
		TreePath path = componentSelectionModel.getSelectionPath();
		if (path == null)
			return null;
		tree.scrollPathToVisible(path);

		return (RocketComponent) path.getLastPathComponent();
	}


	/**
	 * Creates the menu for the window.
	 */
	private void createMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		JMenuItem item;

		////  File
		menubar.add( FileMenuFactory.makeFileMenu(this));
		////  Edit
		menu = new JMenu(trans.get("main.menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		//// Rocket editing
		menu.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.menu.Rocketedt"));
		menubar.add(menu);


		Action action = UndoRedoAction.newUndoAction(document);
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_U);
		//// Undo the previous operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.undo.desc"));

		menu.add(item);

		action = UndoRedoAction.newRedoAction(document);
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		item.setMnemonic(KeyEvent.VK_R);
		//// Redo the previously undone operation
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.redo.desc"));
		menu.add(item);

		menu.addSeparator();


		item = new JMenuItem(actions.getCutAction());
		menu.add(item);

		item = new JMenuItem(actions.getCopyAction());
		menu.add(item);

		item = new JMenuItem(actions.getPasteAction());
		menu.add(item);

		item = new JMenuItem(actions.getDeleteAction());
		menu.add(item);

		menu.addSeparator();



		item = new JMenuItem(trans.get("main.menu.edit.resize"));
		item.setIcon(Icons.EDIT_SCALE);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.resize.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Scale... selected");
				ScaleDialog dialog = new ScaleDialog(document, getSelectedComponent(), getRootWindow());
				dialog.setVisible(true);
				dialog.dispose();
			}
		});
		menu.add(item);



		//// Preferences
		item = new JMenuItem(trans.get("main.menu.edit.preferences"));
		item.setIcon(Icons.PREFERENCES);
		//// Setup the application preferences
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.edit.preferences.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Preferences selected");
				PreferencesDialog.showPreferences(getRootWindow());
			}
		});
		menu.add(item);

		//// Edit Component Preset File

		if (System.getProperty("openrocket.preseteditor.menu") != null) {
			item = new JMenuItem(trans.get("main.menu.edit.editpreset"));
			item.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFrame dialog = new JFrame();
					dialog.getContentPane().add(new ComponentPresetEditor(dialog));
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setVisible(true);
				}
			});
			menu.add(item);
		}

		////  Analyze
		menu = new JMenu(trans.get("main.menu.analyze"));
		menu.setMnemonic(KeyEvent.VK_A);
		//// Analyzing the rocket
		menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.desc"));
		menubar.add(menu);

		//// Component analysis
		item = new JMenuItem(trans.get("main.menu.analyze.componentAnalysis"), KeyEvent.VK_C);
		//// Analyze the rocket components separately
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.componentAnalysis.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Component analysis selected");
				ComponentAnalysisDialog.showDialog(rocketpanel);
			}
		});
		menu.add(item);

		//// Optimize
		item = new JMenuItem(trans.get("main.menu.analyze.optimization"), KeyEvent.VK_O);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.optimization.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Rocket optimization selected");
				new GeneralOptimizationDialog(document, getRootWindow()).setVisible(true);
			}
		});
		menu.add(item);

		//// Custom expressions
		item = new JMenuItem(trans.get("main.menu.analyze.customExpressions"), KeyEvent.VK_E);
		item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.analyze.customExpressions.desc"));
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("Custom expressions selected");
				new CustomExpressionDialog(document, getRootWindow()).setVisible(true);
			}
		});
		menu.add(item);

		////  Debug
		// (shown if openrocket.debug.menu is defined)
		if (System.getProperty("openrocket.debug.menu") != null) {
			menubar.add(DebugMenuFactory.makeDebugMenu(getRootWindow()));
		}

		////  Help
		menubar.add(HelpMenuFactory.makeHelpMenu(this));

		this.setJMenuBar(menubar);
	}

	void openAction() {
		JFileChooser chooser = new JFileChooser();

		chooser.addChoosableFileFilter(FileHelper.ALL_DESIGNS_FILTER);
		chooser.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
		chooser.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
		chooser.setFileFilter(FileHelper.ALL_DESIGNS_FILTER);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		int option = chooser.showOpenDialog(this);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.user("Decided not to open files, option=" + option);
			return;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

		File[] files = chooser.getSelectedFiles();
		log.user("Opening files " + Arrays.toString(files));

		for (File file : files) {
			log.info("Opening file: " + file);
			if (DocumentManager.open(file, getRootWindow())) {
				MRUDesignFile opts = MRUDesignFile.getInstance();
				opts.addFile(file.getAbsolutePath());
			}
		}
	}

	void closeIfReplaceable() {
		// Close previous window if replacing
		if (replaceable && document.isSaved()) {
			// We are replacing the frame, make new window have current location
			BasicFrame newFrame = DocumentManager.getLatestFrame();
			newFrame.rootWindow.setLocation(rootWindow.getLocation());

			log.info("Closing window because it is replaceable");
			closeAction();
		}

	}
	/**
	 * "Save" action.  If the design is new, then this is identical to "Save As", with a default file filter for .ork.
	 * If the rocket being edited previously was opened from a .ork file, then it will be saved immediately to the same
	 * file.  But clicking on 'Save' for an existing design file with a .rkt will bring up a confirmation dialog because
	 * it's potentially a destructive write (loss of some fidelity if it's truly an original Rocksim generated file).
	 *
	 * @return true if the file was saved, false otherwise
	 */
	boolean saveAction() {
		File file = document.getFile();
		if (file == null) {
			log.info("Document does not contain file, opening save as dialog instead");
			return saveAsAction();
		}
		log.info("Saving document to " + file);

		if (FileHelper.ROCKSIM_DESIGN_FILTER.accept(file)) {
			return saveAsRocksim(file);
		}
		return saveAs(file);
	}

	/**
	 * "Save As" action.
	 *
	 * Never should a .rkt file contain an OpenRocket content, or an .ork file contain a Rocksim design.  Regardless of
	 * what extension the user has chosen, it would violate the Principle of Least Astonishment to do otherwise
	 * (and we want to make doing the wrong thing really hard to do).  So always force the appropriate extension.
	 *
	 * This can result in some odd looking filenames (MyDesign.rkt.ork, MyDesign.rkt.ork.rkt, etc.) if the user is
	 * not paying attention, but the user can control that by modifying the filename in the dialog.
	 *
	 * @return true if the file was saved, false otherwise
	 */
	boolean saveAsAction() {
		File file = null;

		StorageOptionChooser storageChooser =
				new StorageOptionChooser(document, document.getDefaultStorageOptions());
		final JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
		chooser.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);

		//Force the file filter to match the file extension that was opened.  Will default to OR if the file is null.
		if (FileHelper.ROCKSIM_DESIGN_FILTER.accept(document.getFile())) {
			chooser.setFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
		}
		else {
			chooser.setFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
		}
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setAccessory(storageChooser);
		if (document.getFile() != null) {
			chooser.setSelectedFile(document.getFile());
		}

		int option = chooser.showSaveDialog(rootWindow);
		if (option != JFileChooser.APPROVE_OPTION) {
			log.user("User decided not to save, option=" + option);
			return false;
		}

		file = chooser.getSelectedFile();
		if (file == null) {
			log.user("User did not select a file");
			return false;
		}

		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		storageChooser.storeOptions(document.getDefaultStorageOptions());

		if (chooser.getFileFilter().equals(FileHelper.ROCKSIM_DESIGN_FILTER)) {
			return saveAsRocksim(file);
		}
		else {
			file = FileHelper.forceExtension(file, "ork");
			return FileHelper.confirmWrite(file, rootWindow) && saveAs(file);
		}
	}

	/**
	 * Perform the writing of the design to the given file in Rocksim format.
	 *
	 * @param file  the chosen file
	 *
	 * @return true if the file was written
	 */
	private boolean saveAsRocksim(File file) {
		file = FileHelper.forceExtension(file, "rkt");
		if (!FileHelper.confirmWrite(file, rootWindow)) {
			return false;
		}

		try {
			new RocksimSaver().save(file, document);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Perform the writing of the design to the given file in OpenRocket format.
	 *
	 * @param file  the chosen file
	 *
	 * @return true if the file was written
	 */
	private boolean saveAs(File file) {
		log.info("Saving document as " + file);
		boolean saved = false;

		if (!StorageOptionChooser.verifyStorageOptions(document, getRootWindow())) {
			// User cancelled the dialog
			log.user("User cancelled saving in storage options dialog");
			return false;
		}


		SaveFileWorker worker = new SaveFileWorker(document, file, ROCKET_SAVER);

		if (!SwingWorkerDialog.runWorker(getRootWindow(), "Saving file",
				"Writing " + file.getName() + "...", worker)) {

			// User cancelled the save
			log.user("User cancelled the save, deleting the file");
			file.delete();
			return false;
		}

		try {
			worker.get();
			document.setFile(file);
			document.setSaved(true);
			saved = true;
			setTitle();
		} catch (ExecutionException e) {

			Throwable cause = e.getCause();

			if (cause instanceof IOException) {
				log.warn("An I/O error occurred while saving " + file, cause);
				JOptionPane.showMessageDialog(rootWindow, new String[] {
						"An I/O error occurred while saving:",
						e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				Reflection.handleWrappedException(e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		return saved;
	}


	boolean closeAction() {
		if (!document.isSaved()) {
			log.info("Confirming whether to save the design");
			ComponentConfigDialog.hideDialog();
			int result = JOptionPane.showConfirmDialog(rootWindow,
					trans.get("BasicFrame.dlg.lbl1") + rocket.getName() +
					trans.get("BasicFrame.dlg.lbl2") + "  " +
					trans.get("BasicFrame.dlg.lbl3"),
					trans.get("BasicFrame.dlg.title"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				// Save
				log.user("User requested file save");
				if (!saveAction()) {
					log.info("File save was interrupted, not closing");
					return false;
				}
			} else if (result == JOptionPane.NO_OPTION) {
				// Don't save: No-op
				log.user("User requested to discard design");
			} else {
				// Cancel or close
				log.user("User cancelled closing, result=" + result);
				return false;
			}
		}

		// Rocket has been saved or discarded
		log.debug("Disposing window");
		// FIXME -
		this.dispose();

		ComponentConfigDialog.hideDialog();
		ComponentAnalysisDialog.hideDialog();

		DocumentManager.removeFrame(this);
		return true;
	}



	/**
	 *
	 */
	public void printAction() {
		Double rotation = rocketpanel.getFigure().getRotation();
		if (rotation == null) {
			rotation = 0d;
		}
		new PrintDialog(getRootWindow(), document, rotation).setVisible(true);
	}

	/**
	 * Open a new design window with a basic rocket+stage.
	 */
	public static void newAction() {
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				log.info("New action initiated");

				Rocket rocket = new Rocket();
				Stage stage = new Stage();
				//// Sustainer
				stage.setName(trans.get("BasicFrame.StageName.Sustainer"));
				rocket.addChild(stage);
				OpenRocketDocument doc = new OpenRocketDocument(rocket);
				doc.setSaved(true);

				BasicFrame frame = BasicFrame.Builder.newInstance(doc);
				frame.replaceable = true;
				frame.rootWindow.setVisible(true);
				// kruland commented this out - I don't like it.
				//ComponentConfigDialog.showDialog(frame, doc, rocket);
			}

		});
	}

	/**
	 * Set the title of the frame, taking into account the name of the rocket, file it
	 * has been saved to (if any) and saved status.
	 */
	private void setTitle() {
		File file = document.getFile();
		boolean saved = document.isSaved();
		String title;

		title = rocket.getName();
		if (file != null) {
			title = title + " (" + file.getName() + ")";
		}
		if (!saved)
			title = "*" + title;

		this.setTitle(title);
	}


}
