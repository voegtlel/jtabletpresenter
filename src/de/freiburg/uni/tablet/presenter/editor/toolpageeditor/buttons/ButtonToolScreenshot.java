/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture.CaptureWrapper;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;

/**
 * @author lukas
 * 
 */
public class ButtonToolScreenshot extends AbstractButtonAction {
	private static final long serialVersionUID = 1L;
	private final boolean _selectScreen;
	private final boolean _rectangleArea;
	private final boolean _hideWindow;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolScreenshot(final IToolPageEditor editor, final String name, final String title, final boolean selectScreen, final boolean rectangleArea, final boolean hideWindow) {
		super(name, editor, title, "/buttons/tool-screenshot.png");
		_selectScreen = selectScreen;
		_rectangleArea = rectangleArea;
		_hideWindow = hideWindow;
	}

	@Override
	public void performLater(final Component component) {
		try {
			BufferedImage imageData = CaptureWrapper.captureScreen(_selectScreen, _rectangleArea, component, _hideWindow);
			if (imageData != null) {
				_editor.getDocumentEditor().setCurrentImage(imageData);
				
				_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
			}
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
