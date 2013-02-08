/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;

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
	 * 
	 * @return the undo action or null, if not existing.
	 */
	IAction getUndoAction();

	/**
	 * @param editor
	 */
	void perform(IDocumentEditor editor);
}
