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
		_editor.getDocumentEditor().setCurrentPageByIndex(
				_editor.getDocumentEditor().getCurrentPageIndex() + 1, true);
	}
}
