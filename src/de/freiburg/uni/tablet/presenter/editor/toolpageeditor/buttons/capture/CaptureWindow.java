package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JWindow;

/**
 * Class for a window for capturing a screenshot.
 * Use {@link CaptureWrapper}.
 */
public class CaptureWindow extends JWindow {
	private static final long serialVersionUID = 1L;
	private BufferedImage _captureImage;
	private boolean _needRectangle;
	private boolean _isSelecting = false;
	
	private boolean _drawFocused;
	
	private Point _captureStart;
	private Rectangle _captureRectangle;
	private Window _owner;

	/**
	 * Create the frame.
	 * @throws AWTException
	 */
	public CaptureWindow(final GraphicsDevice device, final Window owner, final boolean needRectangle) throws AWTException {
		super(owner, device.getDefaultConfiguration());
		_owner = owner;
		setFocusable(true);
		setFocusableWindowState(true);
		setAutoRequestFocus(true);
		_needRectangle = needRectangle;
		setAlwaysOnTop(true);
		setBounds(device.getDefaultConfiguration().getBounds());
		Robot robot = new Robot(device);
		Rectangle bounds = device.getDefaultConfiguration().getBounds();
		// Fixed: On windows 10, bounds.x and bounds.y are required. This was not so on windows 7/8...
		_captureRectangle = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
		_captureImage = robot.createScreenCapture(bounds);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	}
	
	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			onFinish(null);
		} else {
			super.processKeyEvent(e);
		}
	}
	
	@Override
	protected void processMouseMotionEvent(final MouseEvent e) {
		if (_isSelecting) {
			_captureRectangle.x = Math.min(_captureStart.x, e.getX());
			_captureRectangle.y = Math.min(_captureStart.y, e.getY());
			_captureRectangle.width = Math.max(_captureStart.x, e.getX()) - _captureRectangle.x;
			_captureRectangle.height = Math.max(_captureStart.y, e.getY()) - _captureRectangle.y;
			repaint();
		} else {
			super.processMouseMotionEvent(e);
		}
	}
	
	protected void onFinish(final BufferedImage screenshot) {
		_owner.setVisible(false);
		_owner.dispose();
	}
	
	@Override
	protected void processMouseEvent(final MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_ENTERED) {
			_drawFocused = true;
			repaint();
		} else if (e.getID() == MouseEvent.MOUSE_EXITED) {
			_drawFocused = false;
			repaint();
		} else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (_needRectangle) {
					_isSelecting = true;
					_captureStart = e.getPoint();
					_captureRectangle.x = _captureStart.x;
					_captureRectangle.y = _captureStart.y;
					_captureRectangle.width = 0;
					_captureRectangle.height = 0;
					repaint();
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (_needRectangle && _isSelecting && _captureRectangle.width > 0 && _captureRectangle.height > 0) {
					_isSelecting = false;
					repaint();
					BufferedImage screenshot = new BufferedImage(_captureRectangle.width, _captureRectangle.height, BufferedImage.TYPE_INT_ARGB);
					Graphics graphics = screenshot.getGraphics();
					graphics.drawImage(_captureImage, -_captureRectangle.x, -_captureRectangle.y, this);
					graphics.dispose();
					onFinish(screenshot);
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (!_needRectangle) {
					BufferedImage screenshot = new BufferedImage(_captureRectangle.width, _captureRectangle.height, BufferedImage.TYPE_INT_ARGB);
					Graphics graphics = screenshot.getGraphics();
					graphics.drawImage(_captureImage, -_captureRectangle.x, -_captureRectangle.y, this);
					graphics.dispose();
					onFinish(screenshot);
				}
			}
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
		g.drawImage(_captureImage, 0, 0, this);
		if (_drawFocused) {
			g.setColor(_isSelecting?Color.GRAY:Color.RED);
			g.fillRect(0, 0, getWidth(), 5);
			g.fillRect(0, 0, 5, getHeight());
			g.fillRect(getWidth() - 5, 0, 5, getHeight());
			g.fillRect(0, getHeight() - 5, getWidth(), 5);
		}
		if (_isSelecting) {
			g.setColor(Color.RED);
			g.fillRect(_captureRectangle.x - 5, _captureRectangle.y - 5, _captureRectangle.width + 10, 5);
			g.fillRect(_captureRectangle.x - 5, _captureRectangle.y - 5, 5, _captureRectangle.height + 10);
			g.fillRect(_captureRectangle.x + _captureRectangle.width, _captureRectangle.y - 5, 5, _captureRectangle.height + 10);
			g.fillRect(_captureRectangle.x - 5, _captureRectangle.y + _captureRectangle.height, _captureRectangle.width + 10, 5);
		}
	}
}
