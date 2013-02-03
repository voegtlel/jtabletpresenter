/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonOpenFrom extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonOpenFrom(final IToolPageEditor editor) {
		super(editor, R.id.open_from);
	}

	@Override
	public void perform(final Context context) {
		FileHelper.showOpenDialog(context, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("open.defaultExt", "jpd")), null);
	}
}
