/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Color;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonColor extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonColor(final IToolPageEditor editor) {
		super("color", editor, "Color", "/buttons/color-select.png", false, 2, 2);
		this._frame.addItem(new ButtonColorItem(editor, "red", null, "/buttons/color-red.png", editor.getConfig().getColor("color.red", new Color(0xbb0000))), false);
		this._frame.addItem(new ButtonColorItem(editor, "green", null, "/buttons/color-green.png", editor.getConfig().getColor("color.green", new Color(0x00bb00))), false);
		this._frame.addItem(new ButtonColorItem(editor, "blue", null, "/buttons/color-blue.png", editor.getConfig().getColor("color.blue", new Color(0x0000bb))), false);
		this._frame.addItem(new ButtonColorItem(editor, "black", null, "/buttons/color-black.png", editor.getConfig().getColor("color.black", new Color(0x000000))), false);
	}
}
