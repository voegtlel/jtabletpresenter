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
public class ButtonPageDelete extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageDelete(final IToolPageEditor editor) {
		super("delete", editor, "Delete", "/buttons/page-delete");
	}

	@Override
	public void performLater(final Component component) {
		final DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		DocumentPage nextPage = _editor.getDocumentEditor().getDocument().getNextPage(page);
		if (nextPage == null) {
			nextPage = _editor.getDocumentEditor().getDocument().getPreviousPage(page);
		}
		_editor.getDocumentEditor().setCurrentPage(nextPage);
		_editor.getDocumentEditor().getDocument().removePage(page);
	}
}
