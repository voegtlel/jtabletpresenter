/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.actions.ActionGroup;
import de.freiburg.uni.tablet.presenter.actions.AddPageAction;
import de.freiburg.uni.tablet.presenter.actions.AddRenderableAction;
import de.freiburg.uni.tablet.presenter.actions.ChangePageIndexAction;
import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.RemovePageAction;
import de.freiburg.uni.tablet.presenter.actions.RemoveRenderableAction;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class DocumentHistory {
	private final LinkedElementList<IAction> _history = new LinkedElementList<IAction>();
	private LinkedElement<IAction> _top;
	private final DocumentEditor _documentEditor;
	private final DocumentListener _documentListener;
	private ActionGroup _currentActionGroup;

	private boolean _isPerforming = false;

	public DocumentHistory(final DocumentEditor documentEditor) {
		_documentEditor = documentEditor;
		_documentListener = new DocumentListener() {
			@Override
			public void renderableRemoved(final IRenderable renderable,
					final DocumentPageLayer layer) {
				if (!_isPerforming) {
					addAction(new RemoveRenderableAction(layer, renderable));
				}
			}

			@Override
			public void renderableAdded(final IRenderable renderable,
					final DocumentPageLayer layer) {
				if (!_isPerforming) {
					addAction(new AddRenderableAction(layer, renderable));
				}
			}

			@Override
			public void pageRemoved(final DocumentPage prevPage,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new RemovePageAction(prevPage, page));
				}
			}

			@Override
			public void pageInserted(final DocumentPage prevPage,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new AddPageAction(prevPage, page));
				}
			}
		};
		_documentEditor.addListener(new DocumentEditorAdapter() {
			@Override
			public void documentChanged(final Document lastDocument) {
				if (lastDocument != null) {
					lastDocument.removeListener(_documentListener);
				}
				_documentEditor.getDocument().addListener(_documentListener);
			}

			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage) {
				if (!_isPerforming) {
					addAction(new ChangePageIndexAction(_documentEditor
							.getCurrentPage(), lastCurrentPage));
				}
			}
		});
		_documentEditor.getDocument().addListener(_documentListener);
	}

	public void beginActionGroup() {
		if (_currentActionGroup == null) {
			ActionGroup res = new ActionGroup();
			addAction(res);
			_currentActionGroup = res;
		}
	}

	public void endActionGroup() {
		if (_currentActionGroup != null) {
			_currentActionGroup = null;
		}
	}

	public void addAction(final IAction action) {
		if (_currentActionGroup != null) {
			_currentActionGroup.addAction(action);
		} else {
			if (_top == null) {
				_history.addLast(action);
			} else {
				_history.setNext(_top, action);
			}
			_top = _history.getLast();
		}
	}

	public boolean hasUndoAction() {
		return _top != null;
	}

	public boolean hasRedoAction() {
		return _top.getNext() != null;
	}

	public void undo() {
		if (_top != null && !_isPerforming) {
			_isPerforming = true;
			_currentActionGroup = null;
			final IAction undoAction = _top.getData().getUndoAction();
			_top = _top.getPrevious();
			undoAction.perform(_documentEditor);
			_isPerforming = false;
		}
	}

	public void redo() {
		if (_top.getNext() != null && !_isPerforming) {
			_isPerforming = true;
			_currentActionGroup = null;
			_top = _top.getNext();
			_top.getData().perform(_documentEditor);
			_isPerforming = false;
		}
	}
}
