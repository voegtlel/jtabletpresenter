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
public class ButtonEraser extends AbstractButtonSelectTool {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonEraser(final IToolPageEditor editor) {
		super(editor, "Eraser", "/buttons/edit-erase.png");
	}

	@Override
	protected void setSelectedTool(final ITool tool) {
		_editor.setInvertedTool(tool);
	}

	@Override
	protected ITool getSelectedTool() {
		return _editor.getInvertedTool();
	}
}
