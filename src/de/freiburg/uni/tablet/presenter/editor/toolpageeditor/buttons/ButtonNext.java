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
	public void perform(final Component button) {
		try {
			int index = _editor.getDocumentEditor().getCurrentPageIndex();
			DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
			ButtonSaveAs.saveDocumentPage(page, new File("page_" + index + "-" + String.format("%X", page.getId()) + ".jpp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		_editor.getDocumentEditor().setCurrentPageByIndex(
				_editor.getDocumentEditor().getCurrentPageIndex() + 1, true);
	}
}
