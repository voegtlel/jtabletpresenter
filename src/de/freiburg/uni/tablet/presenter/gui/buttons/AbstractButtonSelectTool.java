/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.gui.JPageToolButton;
import de.freiburg.uni.tablet.presenter.gui.JPageToolFrame;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonSelectTool extends AbstractButtonAction {
	private final JPageToolFrame<ITool> _tool;

	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonSelectTool(final IToolPageEditor editor,
			final String text, final String imageResource) {
		super(editor, text, imageResource);
		_tool = new JPageToolFrame<ITool>();
		_tool.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * 2);
		_tool.addValue(
				"Pen",
				"/buttons/edit-scribble.png",
				new ToolScribble(_editor.getToolContainer(), _editor
						.getRenderer(), _editor));
		_tool.addValue(
				"Eraser",
				"/buttons/edit-erase.png",
				new ToolEraser(_editor.getToolContainer(), _editor
						.getRenderer(), _editor));
	}

	@Override
	public void perform(final Component button) {
		final ITool currentSelectedTool = getSelectedTool();
		_tool.setSelectedValue(currentSelectedTool);
		_tool.showAt(button, button.getWidth(), 0);
		final ITool selectedTool = _tool.getSelectedValue();
		if (!selectedTool.equals(currentSelectedTool)) {
			setSelectedTool(selectedTool);
		}
	}

	protected abstract void setSelectedTool(ITool tool);

	protected abstract ITool getSelectedTool();
}
