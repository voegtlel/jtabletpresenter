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
public class ButtonPreferences extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPreferences(final IToolPageEditor editor) {
		super(editor, "Prefs", "/buttons/document-properties.png");
	}

	@Override
	public void perform(final Component button) {
	}
}
