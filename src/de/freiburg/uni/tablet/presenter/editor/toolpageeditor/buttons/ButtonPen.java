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
	 * Creates the action with an editor.
	 */
	public ButtonPen(final IToolPageEditor editor) {
		super(editor, "Pen", "/buttons/edit-scribble.png");
	}

	@Override
	protected void setSelectedTool(final ITool tool) {
		_editor.setNormalTool(tool);
	}

	@Override
	protected ITool getSelectedTool() {
		return _editor.getNormalTool();
	}
}
