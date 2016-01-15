/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Point;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuFrame;

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
	public AbstractButtonMenuTool(final String name, final IToolPageEditor editor,
			final String text, final String imageResource, int baseSize, final int count) {
		this(name, editor, text, imageResource, baseSize, true, true, 1, count);
	}
	
	/**
	 * Creates the action with an editor.
	 */
	public AbstractButtonMenuTool(final String name, final IToolPageEditor editor,
			final String text, final String imageResource, final int baseSize, final boolean wideMode, final boolean hasText,
		    final int countX, final int countY) {
		super(name, editor, text, imageResource);
		_frame = new JPageToolMenuFrame<AbstractButtonAction>();
		int width = (int)(baseSize * (wideMode ? JPageToolButton.WIDTH_WIDE : JPageToolButton.WIDTH_NORMAL));
		_frame.setSize(width * countX,
				(int)(baseSize * (hasText?JPageToolButton.HEIGHT_NORMAL:JPageToolButton.HEIGHT_NOTEXT)) * countY);
	}

	@Override
	public void perform(final Point desiredLocation) {
		_frame.showAt(desiredLocation);
	}
	
	@Override
	public IButtonAction getButton(final String name) {
		IButtonAction result = super.getButton(name);
		if (result != null) {
			return result;
		}
		if (name.startsWith(getName() + ".")) {
			final String subName = name.substring(getName().length() + 1);
			for (AbstractButtonAction ba : _frame.getActions()) {
				result = ba.getButton(subName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	@Override
	public void dispose() {
		_frame.dispose();
	}
}
