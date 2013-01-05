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
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/**
	 * Creates the action with an editor.
	 */
	public ButtonEraser(final IToolPageEditor editor) {
		super("eraser", editor, "Eraser", "/buttons/edit-erase.png");
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
