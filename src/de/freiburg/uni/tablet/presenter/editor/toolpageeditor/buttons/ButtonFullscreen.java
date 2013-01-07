/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonFullscreen extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonFullscreen(final IToolPageEditor editor) {
		super("fullscreen", editor, "Full", "/buttons/view-fullscreen.png");
	}

	@Override
	public void performLater(final Component component) {
		_editor.setFullscreen(!_editor.isFullscreen());
	}
}
