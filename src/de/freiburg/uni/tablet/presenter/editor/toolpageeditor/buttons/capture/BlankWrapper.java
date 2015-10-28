package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Class for a frame for capturing a screenshot.
 *
 */
public class BlankWrapper extends JDialog {
	private static final long serialVersionUID = 1L;
	
	public BlankWrapper(final Window owner, final boolean allScreens) throws AWTException {
		super(owner, "Blank screen");
		setUndecorated(true);
		setAutoRequestFocus(true);
		setModal(true);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		Point center = new Point(owner.getX() + owner.getWidth() / 2, owner.getY() + owner.getHeight() / 2);
		boolean isFirst = true;
		for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (allScreens || graphicsDevice.getDefaultConfiguration().getBounds().contains(center)) {
				final BlankWindow frame = new BlankWindow(graphicsDevice, this, isFirst) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onFinish() {
						BlankWrapper.this.setVisible(false);
						BlankWrapper.this.dispose();
					}
				};
				isFirst = false;
				frame.setVisible(true);
			}
		}
	}
	
	/**
	 * Blanks the screens.
	 * @param owner owner component for modality
	 * @param allScreens if true, all screens will be blanked, otherwise only the screen containing the owner
	 * @return the image or null
	 * @throws AWTException
	 */
	public static void blankScreen(final Component owner, final boolean allScreens) throws AWTException {
		Window ownerWindow = SwingUtilities.windowForComponent(owner);
		BlankWrapper frame = new BlankWrapper(ownerWindow, allScreens);
		frame.setVisible(true);
	}
}
