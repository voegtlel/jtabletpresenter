/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Rectangle;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPreferences extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPreferences(final IToolPageEditor editor) {
		super(editor, "Tools", "/buttons/document-properties.png", 4);
		this._frame.addItem(new ButtonOpenFrom(editor));
		this._frame.addItem(new ButtonSaveAs(editor));
		//this._frame.addItem(new ButtonPreferences(editor));
		this._frame.addItem(new ButtonPen(editor));
		this._frame.addItem(new ButtonEraser(editor));
	}
}
