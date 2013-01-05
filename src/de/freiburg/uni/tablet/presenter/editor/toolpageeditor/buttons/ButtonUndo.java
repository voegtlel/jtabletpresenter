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
public class ButtonUndo extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonUndo(final IToolPageEditor editor) {
		super("undo", editor, "Undo", "/buttons/edit-undo.png");
	}

	@Override
	public void perform(final Component button) {
		if (_editor.getDocumentEditor().getHistory().hasUndoAction()) {
			_editor.getDocumentEditor().getHistory().undo();
			_editor.getPageEditor().clear();
		}
	}
}
