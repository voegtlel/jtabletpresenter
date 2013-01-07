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
		super("tools", editor, "Tools", "/buttons/document-properties.png", 7);
		this._frame.addItem(new ButtonDocumentNew(editor));
		this._frame.addItem(new ButtonOpenFrom(editor));
		this._frame.addItem(new ButtonSaveAs(editor));
		//this._frame.addItem(new ButtonPreferences(editor));
		this._frame.addItem(new ButtonPrimary(editor));
		this._frame.addItem(new ButtonSecondary(editor));
		this._frame.addItem(new ButtonToolPage(editor));
		this._frame.addItem(new ButtonToolDocument(editor));
	}
}
