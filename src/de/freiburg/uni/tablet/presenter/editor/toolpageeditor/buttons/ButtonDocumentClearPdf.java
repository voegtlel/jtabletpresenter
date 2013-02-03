/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonDocumentClearPdf extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonDocumentClearPdf(final IToolPageEditor editor) {
		// TODO: Create button
		super(editor, -1);
	}

	@Override
	public void perform(final Context component) {
		_editor.getDocumentEditor().getDocument().setPdfPages(null, IEditableDocument.PDF_MODE_CLEAR);
	}
}
