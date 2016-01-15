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
	public ButtonToolDocument(final IToolPageEditor editor, final int baseSize) {
		super("document", editor, "Document", "/buttons/tool-document", baseSize, 3);
		this._frame.addItem(new ButtonDocumentNew(editor), baseSize, true);
		this._frame.addItem(new ButtonDocumentClearPdf(editor), baseSize, true);
		this._frame.addItem(new ButtonDocumentOpenPdf(editor), baseSize, true);
	}
}
