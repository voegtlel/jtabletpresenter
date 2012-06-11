/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.gui.IButtonAction;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonAction implements IButtonAction {
	protected final IToolPageEditor _editor;
	private final String _text;
	private final String _imageResource;

	/**
	 * Creates the action with an editor.
	 */
	public AbstractButtonAction(final IToolPageEditor editor,
			final String text, final String imageResource) {
		_editor = editor;
		_text = text;
		_imageResource = imageResource;
	}

	@Override
	public String getText() {
		return _text;
	}

	@Override
	public String getImageResource() {
		return _imageResource;
	}

	@Override
	public Component getControl() {
		return null;
	}

	@Override
	public void perform(final Component button) {
	}
}
