/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonAction implements IButton {
	protected final IToolPageEditor _editor;

	private final int _actionId;

	/**
	 * Creates the action with an editor.
	 * @param undo 
	 */
	public AbstractButtonAction(final IToolPageEditor editor, final int actionId) {
		_editor = editor;
		_actionId = actionId;
	}
	
	@Override
	public boolean perform(final Context context, final int actionId, final int groupActionId) {
		if (_actionId == actionId) {
			perform(context);
			return true;
		}
		return false;
	}
	
	/**
	 * Called if the actionId is correct
	 * @param context
	 */
	public abstract void perform(final Context context);
}
