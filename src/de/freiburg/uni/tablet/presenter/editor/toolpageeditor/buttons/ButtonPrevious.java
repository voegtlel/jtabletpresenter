/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPrevious extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPrevious(final IToolPageEditor editor) {
		super(editor, R.id.previous);
	}

	@Override
	public void perform(final Context context) {
		if (_editor.getConfig().getBoolean("autosave.previous", true)) {
			try {
				FileHelper.autosave(_editor.getDocumentEditor());
			} catch (Exception e) {
			}
		}
		final DocumentPage currentPage = _editor.getDocumentEditor().getCurrentPage();
		final DocumentPage previousPage = _editor.getDocumentEditor().getDocument().getPreviousPage(currentPage);
		if (previousPage != null) {
			_editor.getDocumentEditor().setCurrentPage(previousPage);
		}
	}
}
