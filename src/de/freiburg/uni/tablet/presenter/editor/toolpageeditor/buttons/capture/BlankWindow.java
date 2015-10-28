package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JWindow;

/**
 * Class for a window for showing a blank screen.
 */
public class BlankWindow extends JWindow {
	private static final long serialVersionUID = 1L;

	private Window _owner;

	/**
	 * Create the frame.
	 * @throws AWTException
	 */
	public BlankWindow(final GraphicsDevice device, final Window owner, final boolean requestFocus) throws AWTException {
		super(owner, device.getDefaultConfiguration());
		_owner = owner;
		setFocusable(true);
		setFocusableWindowState(true);
		setAutoRequestFocus(true);
		setAlwaysOnTop(true);
		setBounds(device.getDefaultConfiguration().getBounds());
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
		
		if (requestFocus) {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(final ComponentEvent e) {
					BlankWindow.this.requestFocus();
				}
			});
		}
	}
	
	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			System.out.println("Key Event: " + e);
			onFinish();
			e.consume();
		}
	}
	
	protected void onFinish() {
		_owner.setVisible(false);
		_owner.dispose();
	}
	
	@Override
	protected void processMouseEvent(final MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			System.out.println("Clicked");
			onFinish();
		} else {
			super.processMouseEvent(e);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
