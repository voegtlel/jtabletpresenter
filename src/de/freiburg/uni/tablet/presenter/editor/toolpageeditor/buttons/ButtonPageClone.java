/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import javax.swing.JOptionPane;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPageClone extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageClone(final IToolPageEditor editor) {
		super("clone", editor, "Clone", "/buttons/page-clone.png");
	}

	@Override
	public void perform(final Component button) {
		Object result = JOptionPane.showInputDialog(button, "Page number to insert", "Clone to", JOptionPane.QUESTION_MESSAGE, null, null, Integer.valueOf(_editor.getDocumentEditor().getCurrentPageIndex() + 1).toString());
		if ((result != null) && !result.toString().isEmpty()) {
			int insertIndex = Integer.parseInt(result.toString()) - 1;
			DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
			page.clone(_editor.getDocumentEditor().getDocument());
			DocumentPage prevPage = _editor.getDocumentEditor().getDocument().getPageByIndex(insertIndex - 1, true);
			_editor.getDocumentEditor().getDocument().insertPage(prevPage, page);
			_editor.getDocumentEditor().setCurrentPage(page);
		}
	}
}
