/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.util.ArrayList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.actions.ActionGroup;
import de.freiburg.uni.tablet.presenter.actions.AddPageAction;
import de.freiburg.uni.tablet.presenter.actions.AddRenderableAction;
import de.freiburg.uni.tablet.presenter.actions.ChangePageIndexAction;
import de.freiburg.uni.tablet.presenter.actions.ChangePdfPageAction;
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
	private LinkedElement<IAction> _topNext;
	private final DocumentEditor _documentEditor;
	private final DocumentListener _documentListener;
	private ActionGroup _currentActionGroup;
	
	private final List<DocumentHistoryListener> _listeners = new ArrayList<DocumentHistoryListener>();

	private boolean _isPerforming = false;

	public DocumentHistory(final DocumentEditor documentEditor) {
		_documentEditor = documentEditor;
		_documentListener = new DocumentListener() {
			@Override
			public void renderableRemoved(final IRenderable renderable,
					final DocumentPage layer) {
				if (!_isPerforming) {
					addAction(new RemoveRenderableAction(layer, renderable));
				}
			}

			@Override
			public void renderableAdded(final IRenderable renderable,
					final DocumentPage layer) {
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
			
			@Override
			public void pdfPageChanged(DocumentPage documentPage,
					PdfPageSerializable lastPdfPage) {
				if (!_isPerforming) {
					addAction(new ChangePdfPageAction(documentPage, documentPage.getPdfPage(), lastPdfPage));
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
				addAction(new SetDocumentAction(_documentEditor.getDocument()));
				// Clear history
				_history.clear();
			}
			
			@Override
			public void currentPageChanged(DocumentPage lastCurrentPage,
					DocumentPage lastCurrentBackPage) {
				if (!_isPerforming && (lastCurrentPage != null)) {
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
		if (action.hasUndoAction()) {
			if (_currentActionGroup != null) {
				_currentActionGroup.addAction(action);
			} else {
				if (_top == null) {
					_history.addLast(action);
				} else {
					_history.setNext(_top, action);
				}
				_top = _history.getLast();
				_topNext = null;
			}
		}
		fireActionAdded(action);
	}

	public boolean hasUndoAction() {
		return _top != null;
	}

	public boolean hasRedoAction() {
		return _topNext != null;
	}

	public void undo() {
		if (_top != null && !_isPerforming) {
			_isPerforming = true;
			_currentActionGroup = null;
			final IAction undoAction = _top.getData().getUndoAction();
			_topNext = _top;
			_top = _top.getPrevious();
			undoAction.perform(_documentEditor);
			fireActionPerformed(undoAction);
			_isPerforming = false;
		}
	}

	public void redo() {
		if (_topNext != null && !_isPerforming) {
			_isPerforming = true;
			_currentActionGroup = null;
			_top = _topNext;
			_topNext = _top.getNext();
			_top.getData().perform(_documentEditor);
			fireActionPerformed(_top.getData());
			_isPerforming = false;
		}
	}
	
	protected void fireActionAdded(final IAction action) {
		for (DocumentHistoryListener listener : _listeners) {
			listener.actionAdded(action);
		}
	}
	
	protected void fireActionPerformed(final IAction action) {
		for (DocumentHistoryListener listener : _listeners) {
			listener.actionPerformed(action);
		}
	}
	
	public void removeListener(final DocumentHistoryListener listener) {
		_listeners.remove(listener);
	}
	
	public void addListener(final DocumentHistoryListener listener) {
		_listeners.add(listener);
	}
}
