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
		super("clone", editor, "Clone", "/buttons/page-clone");
	}

	@Override
	public void performLater(final Component component) {
		Object result = JOptionPane.showInputDialog(component, "Page number to insert", "Clone to", JOptionPane.QUESTION_MESSAGE, null, null, Integer.valueOf(_editor.getDocumentEditor().getCurrentPageIndex() + 1).toString());
		if ((result != null) && !result.toString().isEmpty()) {
			final int insertIndex = Integer.parseInt(result.toString()) - 1;
			final DocumentPage page = _editor.getDocumentEditor().getDocument().clonePage(_editor.getDocumentEditor().getCurrentPage());
			final DocumentPage prevPage = _editor.getDocumentEditor().getDocument().getPageByIndex(insertIndex - 1, true);
			_editor.getDocumentEditor().getDocument().insertPage(prevPage, page);
			_editor.getDocumentEditor().setCurrentPage(page);
		}
	}
}
