package net.sf.openrocket.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.main.ComponentAddButtons;

public class FlowingButtonBox extends JPanel implements Scrollable {

	private final int rows;
	private final int maxCols;

	private final int gap;
	private final int extraSpace;

	private JButton[][] buttons;
	private final MigLayout layout;
	private final JViewport viewport;

	private int width, height;

	private static final String BUTTONPARAM = "grow, sizegroup buttons";

	public FlowingButtonBox(int rows, int maxCols, JViewport viewport) {
		this(rows, maxCols, 5, 0, viewport);
	}

	public FlowingButtonBox(int rows, int maxCols, int gap, int extraSpace, JViewport viewport) {
		super();
		this.rows = rows;
		this.maxCols = maxCols;
		this.gap = gap;
		this.extraSpace = extraSpace;
		this.viewport = viewport;

		String constaint = "[min!]";
		for (int i = 1; i < this.maxCols; i++)
			constaint = constaint + this.gap + "[min!]";

		layout = new MigLayout("fill", constaint);
		setLayout(layout);

		buttons = new JButton[this.rows][];

	}

	public void postConstruct() {
		// Get maximum button size
		int w = 0, h = 0;

		for (int row = 0; row < buttons.length; row++) {
			for (int col = 0; col < buttons[row].length; col++) {
				Dimension d = buttons[row][col].getPreferredSize();
				if (d.width > w)
					w = d.width;
				if (d.height > h)
					h = d.height;
			}
		}

		// Set all buttons to maximum size
		width = w;
		height = h;
		Dimension d = new Dimension(width, height);
		for (int row = 0; row < buttons.length; row++) {
			for (int col = 0; col < buttons[row].length; col++) {
				buttons[row][col].setMinimumSize(d);
				buttons[row][col].setPreferredSize(d);
				Component[] children = buttons[row][col].getComponents();
				if ( children != null && children.length > 0 ) {
					buttons[row][col].getComponent(0).validate();
				}
			}
		}

		// Add viewport listener if viewport provided
		if (viewport != null) {
			viewport.addChangeListener(new ChangeListener() {
				private int oldWidth = -1;

				public void stateChanged(ChangeEvent e) {
					Dimension d = FlowingButtonBox.this.viewport.getExtentSize();
					if (d.width != oldWidth) {
						oldWidth = d.width;
						flowButtons();
					}
				}
			});
		}

		add(new JPanel(), "grow");
	}

	/**
	 * Adds a row of buttons to the panel.
	 * @param label  Label placed before the row
	 * @param row    Row number
	 * @param b      List of ComponentButtons to place on the row
	 */
	public void addButtonRow(String label, int row, JButton... b) {
		if ( label != null ) {
			if (row > 0)
				add(new JLabel(label), "span, gaptop unrel, wrap");
			else
				add(new JLabel(label), "span, gaptop 0, wrap");
		}		
		int col = 0;
		buttons[row] = new JButton[b.length];

		for (int i = 0; i < b.length; i++) {
			buttons[row][col] = b[i];
			if (i < b.length - 1)
				add(b[i], BUTTONPARAM);
			else
				add(b[i], BUTTONPARAM + ", wrap");
			col++;
		}
	}


	/**
	 * Flows the buttons in all rows of the panel.  If a button would come too close
	 * to the right edge of the viewport, "newline" is added to its constraints flowing 
	 * it to the next line.
	 */
	private void flowButtons() {
		if (viewport == null)
			return;

		int w;

		Dimension d = viewport.getExtentSize();

		for (int row = 0; row < buttons.length; row++) {
			w = 0;
			for (int col = 0; col < buttons[row].length; col++) {
				w += gap + width;
				String param = BUTTONPARAM + ",width " + width + "!,height " + height + "!";

				if (w + extraSpace > d.width) {
					param = param + ",newline";
					w = gap + width;
				}
				if (col == buttons[row].length - 1)
					param = param + ",wrap";
				layout.setComponentConstraints(buttons[row][col], param);
			}
		}
		revalidate();
	}

	/////////  Scrolling functionality

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL)
			return visibleRect.height * 8 / 10;
		return 10;
	}


	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

}
