/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

/**
 * Interface for a general action
 * 
 * @author lukas
 * 
 */
public interface IAction {
	/**
	 * Returns, if this action has an undo action.
	 * 
	 * @return true, if an undo action exists
	 */
	boolean hasUndoAction();

	/**
	 * Gets the inverse action for this action.
	 * 
	 * @return the undo action or null, if not existing.
	 */
	IAction getUndoAction();

	/**
	 * Performs the action
	 */
	void perform();
}
