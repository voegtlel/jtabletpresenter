/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

/**
 * @author lukas
 * 
 */
public class ButtonColor implements IButton {
	private final IToolPageEditor _editor;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonColor(final IToolPageEditor editor) {
		_editor = editor;
	}
	
	@Override
	public boolean perform(Context context, int actionId, int groupActionId) {
		if (actionId == R.id.color_red
				|| actionId == R.id.color_green
				|| actionId == R.id.color_blue
				|| actionId == R.id.color_black) {
			int newColor;
			if (actionId == R.id.color_red) {
				newColor = _editor.getConfig().getColor("color.red", 0xffbb0000);
			} else if (actionId == R.id.color_green) {
				newColor = _editor.getConfig().getColor("color.green", 0xff00bb00);
			} else if (actionId == R.id.color_blue) {
				newColor = _editor.getConfig().getColor("color.blue", 0xff0000bb);
			} else if (actionId == R.id.color_black) {
				newColor = _editor.getConfig().getColor("color.black", 0xff000000);
			} else {
				throw new IllegalStateException("Invalid code");
			}
			if (newColor != _editor.getDocumentEditor().getCurrentPen()
					.getColor()) {
				_editor.getDocumentEditor().setCurrentPen(
						new SolidPen(_editor.getDocumentEditor().getCurrentPen()
								.getThickness(), newColor));
			}
			return true;
		}
		return false;
	}
}
