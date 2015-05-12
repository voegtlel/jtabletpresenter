/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.BitmapImageData;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture.CaptureWrapper;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;

/**
 * @author lukas
 * 
 */
public class ButtonToolScreenshot extends AbstractButtonAction {
	private static final long serialVersionUID = 1L;
	
	public static final int MODE_CURRENT_SCREEN = 0;
	public static final int MODE_SELECT_SCREEN = 1;
	public static final int MODE_SELECT_RECTANGLE = 2;
	
	private final int _mode;

	private boolean _hideWindow;

	private boolean _asBackground;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolScreenshot(final IToolPageEditor editor, final String name, final String title, final int mode, final boolean hideWindow, final boolean asBackground) {
		super(name, editor, title, "/buttons/tool-screenshot.png");
		_mode = mode;
		_hideWindow = hideWindow;
		_asBackground = asBackground;
	}

	@Override
	public void performLater(final Component component) {
		try {
			BufferedImage imageData = CaptureWrapper.captureScreen(_mode == MODE_SELECT_SCREEN || _mode == MODE_SELECT_RECTANGLE, _mode == MODE_SELECT_RECTANGLE, component, _hideWindow);
			if (imageData != null) {
				if (_asBackground) {
					DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
					page.setBackgroundEntity(new BitmapImageData(page.getParent(), imageData));
				} else {
					_editor.getDocumentEditor().setCurrentImage(imageData);
					
					_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
				}
			}
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
