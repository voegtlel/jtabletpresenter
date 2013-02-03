/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPageClone extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageClone(final IToolPageEditor editor) {
		super(editor, R.id.page_clone);
	}
	
	@Override
	public void perform(final Context context) {
		// TODO: implement
		/*Object result = JOptionPane.showInputDialog(component, "Page number to insert", "Clone to", JOptionPane.QUESTION_MESSAGE, null, null, Integer.valueOf(_editor.getDocumentEditor().getCurrentPageIndex() + 1).toString());
		if ((result != null) && !result.toString().isEmpty()) {
			final int insertIndex = Integer.parseInt(result.toString()) - 1;
			final DocumentPage page = _editor.getDocumentEditor().getDocument().clonePage(_editor.getDocumentEditor().getCurrentPage());
			final DocumentPage prevPage = _editor.getDocumentEditor().getDocument().getPageByIndex(insertIndex - 1, true);
			_editor.getDocumentEditor().getDocument().insertPage(prevPage, page);
			_editor.getDocumentEditor().setCurrentPage(page);
		}*/
	}
}
