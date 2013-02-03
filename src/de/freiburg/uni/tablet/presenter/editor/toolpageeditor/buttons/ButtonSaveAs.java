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
public class ButtonSaveAs extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonSaveAs(final IToolPageEditor editor) {
		super(editor, R.id.save_as);
	}
	
	
	@Override
	public void perform(final Context context) {
		FileHelper.showSaveDialog(context, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("save.defaultExt", "jpd")), null);
	}
}
