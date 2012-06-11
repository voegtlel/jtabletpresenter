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
public class ButtonPen extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonPen(final IToolPageEditor editor) {
		super(editor, "Pen", "/buttons/edit-scribble.png");
	}

	@Override
	public void perform(final Component button) {
	}
}
