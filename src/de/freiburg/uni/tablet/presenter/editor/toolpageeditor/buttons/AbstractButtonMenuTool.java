/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuFrame;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuSelectFrame;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonMenuTool extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final JPageToolMenuFrame<AbstractButtonAction> _frame;

	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonMenuTool(final IToolPageEditor editor,
			final String text, final String imageResource, int count) {
		super(editor, text, imageResource);
		_frame = new JPageToolMenuFrame<AbstractButtonAction>();
		_frame.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * count);
	}

	@Override
	public void perform(final Component button) {
		_frame.showAt(button, button.getWidth(), 0);
	}
}
