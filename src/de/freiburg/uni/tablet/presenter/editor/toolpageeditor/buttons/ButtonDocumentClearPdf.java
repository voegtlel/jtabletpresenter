/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.document.ServerDocument;
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
		_editor.getDocumentEditor().getDocument().setPdfPages(null, ServerDocument.PDF_MODE_CLEAR);
	}
}
