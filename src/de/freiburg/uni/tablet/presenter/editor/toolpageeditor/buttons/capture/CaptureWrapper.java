package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Class for a frame for capturing a screenshot.
 *
 */
public class CaptureWrapper extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage _screenshot;
	
	public CaptureWrapper(final Window owner, final boolean selectRectangle) throws AWTException {
		super(owner, "Capture screen");
		setUndecorated(true);
		setAutoRequestFocus(true);
		setModal(true);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		boolean isFirst = true;
		for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			final CaptureWindow frame = new CaptureWindow(graphicsDevice, this, selectRectangle, isFirst) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onFinish(final BufferedImage screenshot) {
					_screenshot = screenshot;
					CaptureWrapper.this.setVisible(false);
					CaptureWrapper.this.dispose();
				}
			};
			isFirst = false;
			frame.setVisible(true);
		}
	}
	
	public BufferedImage getScreenshot() {
		return _screenshot;
	}
	
	/**
	 * Captures the screen.
	 * @param selectScreen if true, a tool for selecting the screen to shoot is used
	 * @param selectRectangle if true, the tool is extended with an area select
	 * @param owner owner component for modality
	 * @param hideDelay time in ms to delay for hiding the window
	 * @return the image or null
	 * @throws AWTException
	 */
	public static BufferedImage captureScreen(final boolean selectScreen, final boolean selectRectangle, final Component owner, final boolean hideOwner, final int hideDelay) throws AWTException {
		if (selectScreen) {
			Window ownerWindow = SwingUtilities.windowForComponent(owner);
			if (hideOwner) {
				ownerWindow.setVisible(false);
				// Annoying, but needed to skip transition effects of hiding the window in some OS
				try {
					Thread.sleep(hideDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			CaptureWrapper frame = new CaptureWrapper(ownerWindow, selectRectangle);
			if (hideOwner) {
				ownerWindow.setVisible(true);
			}
			frame.setVisible(true);
			return frame.getScreenshot();
		} else {
			Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
			for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				if (graphicsDevice.getDefaultConfiguration().getBounds().contains(cursorLocation)) {
					// This is the active screen
					Robot robot = new Robot(graphicsDevice);
					Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
					Rectangle captureRectangle = new Rectangle(bounds.width, bounds.height);
					BufferedImage captureImage = robot.createScreenCapture(captureRectangle);
					BufferedImage screenshot = new BufferedImage(captureRectangle.width, captureRectangle.height, BufferedImage.TYPE_INT_ARGB);
					Graphics graphics = screenshot.getGraphics();
					graphics.drawImage(captureImage, -captureRectangle.x, -captureRectangle.y, owner);
					graphics.dispose();
					return screenshot;
				}
			}
		}
		return null;
	}
}
