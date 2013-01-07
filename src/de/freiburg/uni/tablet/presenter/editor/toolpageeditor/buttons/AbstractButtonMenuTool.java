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
			final String text, final String imageResource, int count) {
		super(name, editor, text, imageResource);
		_frame = new JPageToolMenuFrame<AbstractButtonAction>();
		_frame.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * count);
	}

	@Override
	public void perform(final Point desiredLocation) {
		_frame.showAt(desiredLocation);
	}
	
	@Override
	public IButtonAction getButton(String name) {
		IButtonAction result = super.getButton(name);
		if (result != null) {
			return result;
		}
		if (name.startsWith(getName() + ".")) {
			String subName = name.substring(getName().length() + 1);
			for (AbstractButtonAction ba : _frame.getActions()) {
				result = ba.getButton(subName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
}
