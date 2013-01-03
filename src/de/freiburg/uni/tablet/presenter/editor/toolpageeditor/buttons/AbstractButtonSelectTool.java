/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuSelectFrame;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonSelectTool extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPageToolMenuSelectFrame<ITool> _tool;

	protected ToolScribble _toolScribble;

	protected ToolEraser _toolEraser;

	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonSelectTool(final IToolPageEditor editor,
			final String text, final String imageResource) {
		super(editor, text, imageResource);
		_tool = new JPageToolMenuSelectFrame<ITool>();
		_tool.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * 2);
		_toolScribble = new ToolScribble(_editor);
		_toolEraser = new ToolEraser(_editor);
		_tool.addValue("Pen", "/buttons/edit-scribble.png", _toolScribble);
		_tool.addValue("Eraser", "/buttons/edit-erase.png", _toolEraser);
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
