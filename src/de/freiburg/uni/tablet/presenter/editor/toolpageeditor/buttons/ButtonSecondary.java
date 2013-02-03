/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;

/**
 * @author lukas
 * 
 */
public class ButtonSecondary extends AbstractButtonSelectTool {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonSecondary(final IToolPageEditor editor) {
		super(R.id.tool_secondary_group, editor);
		setSelectedTool(_toolEraser);
	}

	@Override
	public void setSelectedTool(final ITool tool) {
		_editor.getPageEditor().setInvertedTool(tool);
	}

	@Override
	public ITool getSelectedTool() {
		return _editor.getPageEditor().getInvertedTool();
	}
}
