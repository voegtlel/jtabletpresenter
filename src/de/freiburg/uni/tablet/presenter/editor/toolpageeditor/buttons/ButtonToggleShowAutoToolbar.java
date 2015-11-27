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
public class ButtonToggleShowAutoToolbar extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToggleShowAutoToolbar(final IToolPageEditor editor) {
		super("toggleAutoToolbar", editor, "Auto Toolbar", "/buttons/toggle-toolbar.png");
	}

	@Override
	public void performLater(final Component component) {
		_editor.setAutoToolbarVisible(!_editor.isAutoToolbarVisible());
	}
}
