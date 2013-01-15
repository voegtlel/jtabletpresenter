/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

/**
 * Interface for a general action
 * 
 * @author lukas
 * 
 */
public interface IAction extends IBinarySerializable {
	/**
	 * Returns, if this action has an undo action.
	 * 
	 * @return true, if an undo action exists
	 */
	boolean hasUndoAction();

	/**
	 * Gets the inverse action for this action.
	 * @param clientId TODO
	 * 
	 * @return the undo action or null, if not existing.
	 */
	IAction getUndoAction(int clientId);

	/**
	 * @param editor
	 */
	void perform(DocumentEditor editor);
	
	/**
	 * Gets the client id of the message source
	 * @return
	 */
	int getClientId();

	/**
	 * Checks if the action consequences a redraw
	 * @param editor
	 * @return
	 */
	boolean mustRedraw(DocumentEditor editor);
}
