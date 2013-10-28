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
public class ButtonTools extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonTools(final IToolPageEditor editor) {
		super("tools", editor, "Tools", "/buttons/document-properties.png", 8);
		this._frame.addItem(new ButtonDocumentNew(editor), true);
		this._frame.addItem(new ButtonOpenFrom(editor), true);
		this._frame.addItem(new ButtonSaveAs(editor), true);
		this._frame.addItem(new ButtonToggleShowToolbar(editor), true);
		//this._frame.addItem(new ButtonPreferences(editor), true);
		this._frame.addItem(new ButtonPrimary(editor), true);
		this._frame.addItem(new ButtonSecondary(editor), true);
		this._frame.addItem(new ButtonToolPage(editor), true);
		this._frame.addItem(new ButtonToolDocument(editor), true);
	}
}
