/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonDocumentClearPdf extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonDocumentClearPdf(final IToolPageEditor editor) {
		super("clearPdf", editor, "Clear PDF", "/buttons/document-clear-pdf.png");
	}

	@Override
	public void performLater(final Component component) {
		_editor.getDocumentEditor().getFrontDocument().setPdfPages(null, IEditableDocument.PDF_MODE_CLEAR);
	}
}
