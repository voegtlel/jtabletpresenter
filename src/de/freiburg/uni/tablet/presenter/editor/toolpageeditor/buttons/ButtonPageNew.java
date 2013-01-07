/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPageNew extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageNew(final IToolPageEditor editor) {
		super("new", editor, "New", "/buttons/page-new.png");
	}

	@Override
	public void performLater(final Component component) {
		DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		DocumentPage newPage = _editor.getDocumentEditor().getDocument().insertPage(page);
		_editor.getDocumentEditor().setCurrentPage(newPage);
	}
}
