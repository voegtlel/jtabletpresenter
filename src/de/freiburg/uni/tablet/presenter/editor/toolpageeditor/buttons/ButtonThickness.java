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
public class ButtonThickness extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonThickness(final IToolPageEditor editor) {
		super("thickness", editor, "Thickness", "/buttons/thickness-select.png", false, 1, 3);
		this._frame.addItem(new ButtonThicknessItem(editor, "thin", null, "/buttons/thickness-thin.png", editor.getConfig().getFloat("thickness.thin", 1.0f)), false);
		this._frame.addItem(new ButtonThicknessItem(editor, "medium", null, "/buttons/thickness-medium.png", editor.getConfig().getFloat("thickness.medium", 2.0f)), false);
		this._frame.addItem(new ButtonThicknessItem(editor, "thick", null, "/buttons/thickness-thick.png", editor.getConfig().getFloat("thickness.thick", 3.0f)), false);
	}
}
