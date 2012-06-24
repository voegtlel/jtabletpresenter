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
	 * Creates the action with an editor.
	 */
	public ButtonPrevious(final IToolPageEditor editor) {
		super(editor, "Prev", "/buttons/go-previous.png");
	}

	@Override
	public void perform(final Component button) {
		if (_editor.getPageIndex() > 0) {
			_editor.setPageIndex(_editor.getPageIndex() - 1);
		}
	}
}
