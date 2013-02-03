package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;

public interface IButton {

	/**
	 * Performs the action if the actionId fits
	 * 
	 * @param context
	 * @param actionId
	 * @param groupActionId
	 * @return true if the action was consumed
	 */
	boolean perform(Context context, int actionId, int groupActionId);

}
