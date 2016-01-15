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
public class ButtonRedo extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonRedo(final IToolPageEditor editor) {
		super("redo", editor, "Redo", "/buttons/edit-redo");
	}

	@Override
	public void performLater(final Component component) {
		if (_editor.getDocumentEditor().getHistory().hasRedoAction()) {
			_editor.getDocumentEditor().getHistory().redo();
		}
	}
}
