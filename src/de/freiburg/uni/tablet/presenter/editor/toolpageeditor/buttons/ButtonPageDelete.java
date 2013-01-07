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
		super("delete", editor, "Delete", "/buttons/page-delete.png");
	}

	@Override
	public void performLater(final Component component) {
		DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		int index = _editor.getDocumentEditor().getCurrentPageIndex() + 1;
		_editor.getDocumentEditor().setCurrentPageByIndex(index, true);
		_editor.getDocumentEditor().getDocument().removePage(page);
	}
}
