/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonNext extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonNext(final IToolPageEditor editor) {
		super(editor, "Next", "/buttons/go-next.png");
	}

	@Override
	public void perform(final Component button) {
		_editor.setPageIndex(_editor.getPageIndex() + 1);
	}
}
