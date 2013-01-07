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
public class ButtonToggleShowToolbar extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToggleShowToolbar(final IToolPageEditor editor) {
		super("toggleToolbar", editor, "Toolbar", "/buttons/toggle-toolbar.png");
	}

	@Override
	public void performLater(final Component component) {
		_editor.setToolbarVisible(!_editor.isToolbarVisible());
	}
}
