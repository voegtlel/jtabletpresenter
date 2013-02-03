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
public class ButtonPageClear extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageClear(final IToolPageEditor editor) {
		super(editor, R.id.page_clear);
	}

	@Override
	public void perform(final Context component) {
		_editor.getDocumentEditor().getHistory().beginActionGroup();
		DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		page.clear();
		_editor.getDocumentEditor().getDocument().clear();
		_editor.getDocumentEditor().getHistory().endActionGroup();
	}
}
