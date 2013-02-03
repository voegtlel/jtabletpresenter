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
public class ButtonPageDelete extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageDelete(final IToolPageEditor editor) {
		super(editor, R.id.page_delete);
	}

	@Override
	public void perform(final Context component) {
		final DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		int index = _editor.getDocumentEditor().getCurrentPageIndex() + 1;
		_editor.getDocumentEditor().setCurrentPageByIndex(index, true);
		_editor.getDocumentEditor().getDocument().removePage(page);
	}
}
