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
	 * Creates the action with an editor.
	 */
	public ButtonUndo(final IToolPageEditor editor) {
		super(editor, "Undo", "/buttons/edit-undo.png");
	}

	@Override
	public void perform(final Component button) {
	}
}
