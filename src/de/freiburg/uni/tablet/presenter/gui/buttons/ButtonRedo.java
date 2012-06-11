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
public class ButtonRedo extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonRedo(final IToolPageEditor editor) {
		super(editor, "Redo", "/buttons/edit-redo.png");
	}

	@Override
	public void perform(final Component button) {
	}
}
