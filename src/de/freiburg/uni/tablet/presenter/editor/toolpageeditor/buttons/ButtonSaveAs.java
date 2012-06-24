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
public class ButtonSaveAs extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonSaveAs(final IToolPageEditor editor) {
		super(editor, "Save", "/buttons/document-save-as.png");
	}

	@Override
	public void perform(final Component button) {
		// _editor.serialize();
	}
}
