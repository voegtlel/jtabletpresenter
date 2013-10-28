/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;

/**
 * @author lukas
 * 
 */
public class ButtonToolItem extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ITool _tool;

	private AbstractButtonSelectTool _selectTool;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolItem(final IToolPageEditor editor, final AbstractButtonSelectTool selectTool, final String name, final String text, final String imageResource, final ITool tool) {
		super(name, editor, text, imageResource);
		_selectTool = selectTool;
		_tool = tool;
	}

	@Override
	public void performLater(final Component component) {
		_selectTool.setSelectedTool(_tool);
	}

	public ITool getTool() {
		return _tool;
	}
}
