/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.actions.AddPageAction;
import de.freiburg.uni.tablet.presenter.actions.AddRenderableAction;
import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.RemovePageAction;
import de.freiburg.uni.tablet.presenter.actions.RemoveRenderableAction;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class PageHistory {
	private final LinkedElementList<IAction> _history = new LinkedElementList<IAction>();
	private LinkedElement<IAction> _top;
	private final DocumentListener _listener;
	private Document _document;

	public PageHistory() {
		_listener = new DocumentListener() {
			@Override
			public void renderableRemoved(final IRenderable renderable,
					final DocumentPageLayer layer) {
				addAction(new RemoveRenderableAction(layer, renderable));
			}

			@Override
			public void renderableAdded(final IRenderable renderable,
					final DocumentPageLayer layer) {
				addAction(new AddRenderableAction(layer, renderable));
			}

			@Override
			public void pageRemoved(final DocumentPage prevPage,
					final DocumentPage page) {
				addAction(new RemovePageAction(prevPage, page));
			}

			@Override
			public void pageInserted(final DocumentPage prevPage,
					final DocumentPage page) {
				addAction(new AddPageAction(prevPage, page));
			}
		};
	}

	public void setDocument(final Document document) {
		if (_document != null) {
			_document.removeListener(_listener);
		}
		_document = document;
		if (_document != null) {
			_document.addListener(_listener);
		}
	}

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
