/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Point;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;

/**
 * Class for a wrapper for an actual action button
 * @author lukas
 * 
 */
public class ButtonWrapper extends AbstractButtonAction {
	private static final long serialVersionUID = 1L;
	private IButtonAction _srcButton;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonWrapper(final IToolPageEditor editor, final IButtonAction srcButton) {
		super("wrapper", editor, srcButton.getText(), srcButton.getImageResource());
		_srcButton = srcButton;
	}
	
	@Override
	public void perform(final Point desiredLocation) {
		_srcButton.perform(desiredLocation);
	}
}
