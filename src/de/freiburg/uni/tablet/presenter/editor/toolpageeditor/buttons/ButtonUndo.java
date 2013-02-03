/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
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
		super(editor, R.id.undo);
	}

	@Override
	public void perform(final Context context) {
		if (_editor.getDocumentEditor().getHistory().hasUndoAction()) {
			_editor.getDocumentEditor().getHistory().undo();
		}
	}
}
