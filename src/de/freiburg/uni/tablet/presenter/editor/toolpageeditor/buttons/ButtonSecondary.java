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
public class ButtonSecondary extends AbstractButtonSelectTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/**
	 * Creates the action with an editor.
	 */
	public ButtonSecondary(final IToolPageEditor editor) {
		super("secondary", editor, "Secondary", "/buttons/edit-erase.png");
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
