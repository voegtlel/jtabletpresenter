/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;

/**
 * @author lukas
 * 
 */
public class ButtonPen extends AbstractButtonSelectTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPen(final IToolPageEditor editor) {
		super("pen", editor, "Pen", "/buttons/edit-scribble.png");
		setSelectedTool(_toolScribble);
	}

	@Override
	public void setSelectedTool(final ITool tool) {
		_editor.getPageEditor().setNormalTool(tool);
	}

	@Override
	public ITool getSelectedTool() {
		return _editor.getPageEditor().getNormalTool();
	}
}
