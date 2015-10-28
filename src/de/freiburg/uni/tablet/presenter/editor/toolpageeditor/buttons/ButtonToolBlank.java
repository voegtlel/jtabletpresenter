/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.AWTException;
import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.capture.BlankWrapper;

/**
 * @author lukas
 * 
 */
public class ButtonToolBlank extends AbstractButtonAction {
	private static final long serialVersionUID = 1L;
	
	private final boolean _allScreens;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolBlank(final IToolPageEditor editor, final String name, final String title, final boolean allScreens) {
		super(name, editor, title, "/buttons/tool-blank.png");
		_allScreens = allScreens;
	}

	@Override
	public void performLater(final Component component) {
		try {
			BlankWrapper.blankScreen(component, _allScreens);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
