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
public class ButtonNext extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonNext(final IToolPageEditor editor) {
		super(editor, R.id.next);
	}

	@Override
	public void perform(final Context context) {
		if (_editor.getConfig().getBoolean("autosave.next", true)) {
			try {
				FileHelper.autosave(_editor.getDocumentEditor());
			} catch (Exception e) {
			}
		}
		final int currentPageIndex = _editor.getDocumentEditor().getCurrentPageIndex();
		if (currentPageIndex < _editor.getDocumentEditor().getMaxPageCount() - 1) {
			final DocumentPage currentPage = _editor.getDocumentEditor().getCurrentPage();
			final DocumentPage nextPage = _editor.getDocumentEditor().getDocument().getNextPage(currentPage, true);
			_editor.getDocumentEditor().setCurrentPage(nextPage);
		}
	}
}
