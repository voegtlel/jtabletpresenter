package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.actions.IAction;

public interface DocumentHistoryListener {
	/**
	 * Fired, when an action was performed (undo or redo)
	 * @param action
	 */
	void actionPerformed(IAction action);

	/**
	 * Fired when an action was added (and thus performed before)
	 * @param action
	 */
	void actionAdded(IAction action);

}
