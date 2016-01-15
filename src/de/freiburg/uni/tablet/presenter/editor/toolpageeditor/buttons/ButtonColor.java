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
	public ButtonColor(final IToolPageEditor editor, int baseSize) {
		super("color", editor, "Color", "/buttons/color-select", baseSize, false, false, editor.getConfig().getBoolean("color.nine",  true)?3:2, editor.getConfig().getBoolean("color.nine",  true)?3:2);
		if (editor.getConfig().getBoolean("color.nine",  true)) {
			this._frame.addItem(new ButtonColorItem(editor, "red", null, "/buttons/color-red", editor.getConfig().getColor("color.red", new Color(0xbb0000))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "orange", null, "/buttons/color-orange", editor.getConfig().getColor("color.orange", new Color(0xee7700))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "yellow", null, "/buttons/color-yellow", editor.getConfig().getColor("color.yellow", new Color(0xdddd00))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "blue", null, "/buttons/color-blue", editor.getConfig().getColor("color.blue", new Color(0x0000bb))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "violet", null, "/buttons/color-violet", editor.getConfig().getColor("color.violet", new Color(0x6600bb))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "green", null, "/buttons/color-green", editor.getConfig().getColor("color.green", new Color(0x00bb00))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "white", null, "/buttons/color-white", editor.getConfig().getColor("color.white", new Color(0xeeeeee))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "grey", null, "/buttons/color-grey", editor.getConfig().getColor("color.grey", new Color(0x666666))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "black", null, "/buttons/color-black", editor.getConfig().getColor("color.black", new Color(0x000000))), baseSize, false);
		} else {
			this._frame.addItem(new ButtonColorItem(editor, "red", null, "/buttons/color-red", editor.getConfig().getColor("color.red", new Color(0xbb0000))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "green", null, "/buttons/color-green", editor.getConfig().getColor("color.green", new Color(0x00bb00))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "blue", null, "/buttons/color-blue", editor.getConfig().getColor("color.blue", new Color(0x0000bb))), baseSize, false);
			this._frame.addItem(new ButtonColorItem(editor, "black", null, "/buttons/color-black", editor.getConfig().getColor("color.black", new Color(0x000000))), baseSize, false);
		}
	}
}
