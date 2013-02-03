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
public class ButtonNext extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonNext(final IToolPageEditor editor) {
		super("next", editor, "Next", "/buttons/go-next.png");
	}

	@Override
	public void performLater(final Component component) {
		if (_editor.getConfig().getBoolean("autosave.next", true)) {
			FileHelper.autosave(_editor.getDocumentEditor());
		}
		final int currentPageIndex = _editor.getDocumentEditor().getCurrentPageIndex();
		if (currentPageIndex < _editor.getDocumentEditor().getMaxPageCount() - 1) {
			final DocumentPage currentPage = _editor.getDocumentEditor().getCurrentPage();
			final DocumentPage nextPage = _editor.getDocumentEditor().getDocument().getNextPage(currentPage, true);
			_editor.getDocumentEditor().setCurrentPage(nextPage);
		}
	}
}
