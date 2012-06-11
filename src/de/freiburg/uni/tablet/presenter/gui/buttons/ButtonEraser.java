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
public class ButtonEraser extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonEraser(final IToolPageEditor editor) {
		super(editor, "Eraser", "/buttons/edit-erase.png");
	}

	@Override
	public void perform(final Component button) {
	}
}
