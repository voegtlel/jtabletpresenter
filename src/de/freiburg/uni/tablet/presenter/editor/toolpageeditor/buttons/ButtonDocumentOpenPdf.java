/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonDocumentOpenPdf extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonDocumentOpenPdf(final IToolPageEditor editor) {
		super("openPdf", editor, "Open PDF", "/buttons/document-open-pdf.png");
	}

	@Override
	public void perform(final Component button) {
		FileHelper.showOpenDialog(button, _editor, FileHelper.FILTER_pdf);
	}
}
