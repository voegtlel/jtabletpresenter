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
public class ButtonOpenFrom extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonOpenFrom(final IToolPageEditor editor) {
		super(editor, "Open", "/buttons/document-open.png");
	}

	@Override
	public void perform(final Component button) {
		// _editor.deserialize();
	}
}
