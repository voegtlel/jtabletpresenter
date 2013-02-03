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
public class ButtonPageNew extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageNew(final IToolPageEditor editor) {
		super(editor, R.id.page_new);
	}

	@Override
	public void perform(final Context context) {
		final DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		final DocumentPage newPage = _editor.getDocumentEditor().getDocument().insertPage(page);
		_editor.getDocumentEditor().setCurrentPage(newPage);
	}
}
