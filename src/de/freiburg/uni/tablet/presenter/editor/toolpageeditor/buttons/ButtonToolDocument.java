/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonToolDocument extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolDocument(final IToolPageEditor editor) {
		super("document", editor, "Document", "/buttons/tool-document.png", 3);
		this._frame.addItem(new ButtonDocumentNew(editor), true);
		this._frame.addItem(new ButtonDocumentClearPdf(editor), true);
		this._frame.addItem(new ButtonDocumentOpenPdf(editor), true);
	}
}
