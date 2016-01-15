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
public class ButtonPrevious extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPrevious(final IToolPageEditor editor) {
		super("previous", editor, "Prev", "/buttons/go-previous");
	}

	@Override
	public void performLater(final Component component) {
		if (_editor.getConfig().getBoolean("autosave.previous", true)) {
			FileHelper.autosave(_editor.getDocumentEditor());
		}
		final DocumentPage currentPage = _editor.getDocumentEditor().getCurrentPage();
		final DocumentPage previousPage = _editor.getDocumentEditor().getDocument().getPreviousPage(currentPage);
		if (previousPage != null) {
			_editor.getDocumentEditor().setCurrentPage(previousPage);
		}
	}
}
