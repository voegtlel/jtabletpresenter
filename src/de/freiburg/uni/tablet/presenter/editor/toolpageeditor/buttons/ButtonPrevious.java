/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

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
		super("previous", editor, "Prev", "/buttons/go-previous.png");
	}

	@Override
	public void perform(final Component button) {
		try {
			int index = _editor.getDocumentEditor().getCurrentPageIndex();
			DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
			ButtonSaveAs.saveDocumentPage(page, new File("page_" + index + "-" + String.format("%X", page.getId()) + ".jpp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		final int currentPageIndex = _editor.getDocumentEditor()
				.getCurrentPageIndex();
		if (currentPageIndex > 0) {
			_editor.getDocumentEditor().setCurrentPageByIndex(
					currentPageIndex - 1, true);
		}
	}
}
