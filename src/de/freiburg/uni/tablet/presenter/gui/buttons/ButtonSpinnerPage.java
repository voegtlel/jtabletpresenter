/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui.buttons;

import java.awt.Component;

import javax.swing.JSpinner;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonSpinnerPage extends AbstractButtonAction {
	private final JSpinner _spinner;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonSpinnerPage(final IToolPageEditor editor) {
		super(editor, null, null);
		_spinner = new JSpinner();
	}

	@Override
	public Component getControl() {
		return _spinner;
	}
}
