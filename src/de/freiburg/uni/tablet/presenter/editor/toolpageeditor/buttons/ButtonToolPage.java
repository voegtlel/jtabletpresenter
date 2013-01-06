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
	public ButtonToolPage(final IToolPageEditor editor) {
		super("page", editor, "Page", "/buttons/tool-page.png", 4);
		this._frame.addItem(new ButtonPageNew(editor));
		this._frame.addItem(new ButtonPageClone(editor));
		this._frame.addItem(new ButtonPageDelete(editor));
		this._frame.addItem(new ButtonPageClear(editor));
	}
}
