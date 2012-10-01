package net.sf.openrocket.gui.main;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.RocketLoader;
import net.sf.openrocket.gui.dialogs.MotorDatabaseLoadingDialog;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.gui.dialogs.WarningDialog;
import net.sf.openrocket.gui.util.OpenFileWorker;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class DocumentManager {

	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	/**
	 * The RocketLoader instance used for loading all rocket designs.
	 */
	private static final RocketLoader ROCKET_LOADER = new GeneralRocketLoader();

	/**
	 * List of currently open frames.  When the list goes empty
	 * it is time to exit the application.
	 */
	private static final ArrayList<BasicFrame> frames = new ArrayList<BasicFrame>();

	public static BasicFrame getLatestFrame() {
		return frames.get(frames.size() - 1);
	}
	
	public static void addFrame( BasicFrame frame ) {
		frames.add(frame);
	}
	
	public static void removeFrame( BasicFrame frame ) {
		frames.remove(frame);
		if (frames.isEmpty()) {
			log.info("Last frame closed, exiting");
			System.exit(0);
		}
	}

	/**
	 * Quit the application.  Confirms saving unsaved designs.  The action of File->Quit.
	 */
	public static void quitAction() {
		log.info("Quit action initiated");
		for (int i = frames.size() - 1; i >= 0; i--) {
			log.debug("Closing frame " + frames.get(i));
			if (!frames.get(i).closeAction()) {
				// Close canceled
				log.info("Quit was cancelled");
				return;
			}
		}
		// Should not be reached, but just in case
		log.error("Should already have exited application");
		System.exit(0);
	}

	/**
	 * Open the specified file from an InputStream in a new design frame.  If an error
	 * occurs, an error dialog is shown and <code>false</code> is returned.
	 *
	 * @param stream	the stream to load from.
	 * @param filename	the file name to display in dialogs (not set to the document).
	 * @param parent	the parent component for which a progress dialog is opened.
	 * @return			whether the file was successfully loaded and opened.
	 */
	private static boolean open(InputStream stream, String filename, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(stream, ROCKET_LOADER);
		return open(worker, filename, null, parent);
	}


	/**
	 * Open the specified file in a new design frame.  If an error occurs, an error
	 * dialog is shown and <code>false</code> is returned.
	 *
	 * @param file		the file to open.
	 * @param parent	the parent component for which a progress dialog is opened.
	 * @return			whether the file was successfully loaded and opened.
	 */
	public static boolean open(File file, Window parent) {
		OpenFileWorker worker = new OpenFileWorker(file, ROCKET_LOADER);
		return open(worker, file.getName(), file, parent);
	}

	/**
	 * Open a file based on a URL.
	 * @param url		the file to open.
	 * @param parent	the parent window for dialogs.
	 * @return			<code>true</code> if opened successfully.
	 */
	public static boolean open(URL url, Window parent) {
		String filename = null;

		// First figure out the file name from the URL

		// Try using URI.getPath();
		try {
			URI uri = url.toURI();
			filename = uri.getPath();
		} catch (URISyntaxException ignore) {
		}

		// Try URL-decoding the URL
		if (filename == null) {
			try {
				filename = URLDecoder.decode(url.toString(), "UTF-8");
			} catch (UnsupportedEncodingException ignore) {
			}
		}

		// Last resort
		if (filename == null) {
			filename = "";
		}

		// Remove path from filename
		if (filename.lastIndexOf('/') >= 0) {
			filename = filename.substring(filename.lastIndexOf('/') + 1);
		}


		// Open the file
		log.info("Opening file from url=" + url + " filename=" + filename);
		try {
			InputStream is = url.openStream();
			DocumentManager.open(is, filename, parent);
		} catch (IOException e) {
			log.warn("Error opening file" + e);
			JOptionPane.showMessageDialog(parent,
					"An error occurred while opening the file " + filename,
					"Error loading file", JOptionPane.ERROR_MESSAGE);
		}

		return false;
	}

	/**
	 * Open the specified file using the provided worker.
	 *
	 * @param worker	the OpenFileWorker that loads the file.
	 * @param filename	the file name to display in dialogs.
	 * @param file		the File to set the document to (may be null).
	 * @param parent
	 * @return
	 */
	private static boolean open(OpenFileWorker worker, String filename, File file, Window parent) {

		MotorDatabaseLoadingDialog.check(parent);

		// Open the file in a Swing worker thread
		log.info("Starting OpenFileWorker");
		if (!SwingWorkerDialog.runWorker(parent, "Opening file", "Reading " + filename + "...", worker)) {
			// User cancelled the operation
			log.info("User cancelled the OpenFileWorker");
			return false;
		}


		// Handle the document
		OpenRocketDocument doc = null;
		try {

			doc = worker.get();

		} catch (ExecutionException e) {

			Throwable cause = e.getCause();

			if (cause instanceof FileNotFoundException) {

				log.warn("File not found", cause);
				JOptionPane.showMessageDialog(parent,
						"File not found: " + filename,
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else if (cause instanceof RocketLoadException) {

				log.warn("Error loading the file", cause);
				JOptionPane.showMessageDialog(parent,
						"Unable to open file '" + filename + "': "
								+ cause.getMessage(),
						"Error opening file", JOptionPane.ERROR_MESSAGE);
				return false;

			} else {

				throw new BugException("Unknown error when opening file", e);

			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		if (doc == null) {
			throw new BugException("Document loader returned null");
		}


		// Show warnings
		WarningSet warnings = worker.getRocketLoader().getWarnings();
		if (!warnings.isEmpty()) {
			log.info("Warnings while reading file: " + warnings);
			WarningDialog.showWarnings(parent,
					new Object[] {
							//// The following problems were encountered while opening
							trans.get("BasicFrame.WarningDialog.txt1") + " " + filename + ".",
							//// Some design features may not have been loaded correctly.
							trans.get("BasicFrame.WarningDialog.txt2")
					},
					//// Warnings while opening file
					trans.get("BasicFrame.WarningDialog.title"), warnings);
		}


		// Set document state
		doc.setFile(file);
		doc.setSaved(true);


		// Open the frame
		log.debug("Opening new frame with the document");
		BasicFrame frame = BasicFrame.Builder.newInstance(doc);

		// FIXME -
		if ( parent != null && parent instanceof BasicFrame ) {
			((BasicFrame)parent).closeIfReplaceable();
		}
		return true;
	}


	/**
	 * Find a currently open BasicFrame containing the specified rocket.  This method
	 * can be used to map a Rocket to a BasicFrame from GUI methods.
	 *
	 * @param rocket the Rocket.
	 * @return		 the corresponding BasicFrame, or <code>null</code> if none found.
	 */
	public static BasicFrame findFrame(Rocket rocket) {
		for (BasicFrame f : frames) {
			if (f.getRocket() == rocket) {
				log.debug("Found frame " + f + " for rocket " + rocket);
				return f;
			}
		}
		log.debug("Could not find frame for rocket " + rocket);
		return null;
	}


	/**
	 * Find a currently open document by the rocket object.  This method can be used
	 * to map a Rocket to OpenRocketDocument from GUI methods.
	 *
	 * @param rocket the Rocket.
	 * @return		 the corresponding OpenRocketDocument, or <code>null</code> if not found.
	 */
	public static OpenRocketDocument findDocument(Rocket rocket) {
		BasicFrame frame = findFrame(rocket);
		if (frame != null) {
			return frame.getDocument();
		} else {
			return null;
		}
	}

}
