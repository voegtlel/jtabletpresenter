/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

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
		super(editor, "Prev", "/buttons/go-previous.png");
	}

	@Override
	public void perform(final Component button) {
		final int currentPageIndex = _editor.getDocumentEditor()
				.getCurrentPageIndex();
		if (currentPageIndex > 0) {
			_editor.getDocumentEditor().setCurrentPageByIndex(
					currentPageIndex - 1, true);
		}
	}
}
