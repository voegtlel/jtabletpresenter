/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class PageHistory {
	private final LinkedElementList<IAction> _history = new LinkedElementList<IAction>();
	private LinkedElement<IAction> _top;

	public void addAction(final IAction action) {
		if (_top == null) {
			_history.addLast(action);
		} else {
			_history.setNext(_top, action);
		}
		_top = _history.getLast();
	}

	public boolean hasUndoAction() {
		return _top != null;
	}

	public boolean hasRedoAction() {
		return _top.getNext() != null;
	}

	public void undo(final DocumentEditor editor) {
		final IAction undoAction = _top.getData().getUndoAction();
		_top = _top.getPrevious();
		undoAction.perform(editor);
	}

	public void redo(final DocumentEditor editor) {
		if (_top.getNext() != null) {
			_top = _top.getNext();
			_top.getData().perform(editor);
		}
	}
}
