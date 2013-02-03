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
public class ButtonRedo extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonRedo(final IToolPageEditor editor) {
		super(editor, R.id.redo);
	}

	@Override
	public void perform(final Context context) {
		if (_editor.getDocumentEditor().getHistory().hasRedoAction()) {
			_editor.getDocumentEditor().getHistory().redo();
		}
	}
}
