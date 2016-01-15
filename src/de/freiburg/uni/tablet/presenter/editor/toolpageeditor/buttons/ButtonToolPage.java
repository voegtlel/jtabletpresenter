/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonToolPage extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonToolPage(final IToolPageEditor editor, final int baseSize) {
		super("page", editor, "Page", "/buttons/tool-page", baseSize, 4);
		this._frame.addItem(new ButtonPageNew(editor), baseSize, true);
		this._frame.addItem(new ButtonPageClone(editor), baseSize, true);
		this._frame.addItem(new ButtonPageDelete(editor), baseSize, true);
		this._frame.addItem(new ButtonPageClear(editor), baseSize, true);
	}
}
