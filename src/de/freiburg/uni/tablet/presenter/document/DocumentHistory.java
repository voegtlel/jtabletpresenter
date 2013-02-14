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
import de.freiburg.uni.tablet.presenter.actions.RenderableModifiedAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;
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
	private final IDocumentEditor _documentEditor;
	private final DocumentListener _documentListener;
	private ActionGroup _currentActionGroup;
	
	private final List<DocumentHistoryListener> _listeners = new ArrayList<DocumentHistoryListener>();

	private boolean _isPerforming = false;

	public DocumentHistory(final IDocumentEditor documentEditor) {
		_documentEditor = documentEditor;
		_documentListener = new DocumentListener() {
			@Override
			public void renderableRemoved(final IRenderable afterRenderable, final IRenderable renderable,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new RemoveRenderableAction(page, afterRenderable, renderable));
				}
			}

			@Override
			public void renderableAdded(final IRenderable afterRenderable, final IRenderable renderable,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new AddRenderableAction(page, afterRenderable, renderable));
				}
			}

			@Override
			public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new RemovePageAction(document, prevPage, page));
				}
			}

			@Override
			public void pageInserted(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new AddPageAction(document, prevPage, page));
				}
			}
			
			@Override
			public void pdfPageChanged(final DocumentPage documentPage,
					final PdfPageSerializable lastPdfPage) {
				if (!_isPerforming) {
					addAction(new ChangePdfPageAction(documentPage, documentPage.getPdfPage(), lastPdfPage));
				}
			}
			
			@Override
			public void renderableModified(final IRenderable renderable,
					final DocumentPage page) {
			}
			
			@Override
			public void renderableModifyEnd(final IRenderable renderable,
					final DocumentPage page) {
				if (!_isPerforming) {
					addAction(new RenderableModifiedAction(renderable));
				}
			}
		};
		_documentEditor.addListener(new DocumentEditorAdapter() {
			@Override
			public void documentChanged(final IClientDocument lastDocument) {
				if (lastDocument != null) {
					lastDocument.removeListener(_documentListener);
				}
				_documentEditor.getFrontDocument().addListener(_documentListener);
				addAction(new SetServerDocumentAction(_documentEditor.getFrontDocument()));
				// Clear history
				clear();
			}
			
			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage,
					final DocumentPage lastCurrentBackPage) {
				if (!_isPerforming && ((lastCurrentPage == null) || (lastCurrentPage.getParent() == _documentEditor.getFrontDocument()))) {
					addAction(new ChangePageIndexAction(_documentEditor
							.getCurrentPage(), lastCurrentPage));
				}
			}
		});
	}
	
	/**
	 * Clear history
	 */
	public void clear() {
		_history.clear();
		_top = null;
		_topNext = null;
	}

	/**
	 * Begin an action group
	 */
	public void beginActionGroup() {
		if (_currentActionGroup == null) {
			_currentActionGroup = new ActionGroup();
		}
	}

	/**
	 * End action group
	 */
	public void endActionGroup() {
		if (_currentActionGroup != null) {
			ActionGroup currentActionGroup = _currentActionGroup;
			_currentActionGroup = null;
			if (!currentActionGroup.isEmpty()) {
				addAction(currentActionGroup);
			}
		}
	}

	/**
	 * Add an action if the action has undo capabilities. Always fires events
	 * @param action
	 */
	public void addAction(final IAction action) {
		if (_currentActionGroup != null) {
			_currentActionGroup.addAction(action);
		} else if (action.hasUndoAction()) {
			if (_top == null) {
				_history.addLast(action);
			} else {
				_history.setNext(_top, action);
			}
			_top = _history.getLast();
			_topNext = null;
		}
		if (!(action instanceof ActionGroup)) {
			System.out.println("Add action " + action.toString());
			fireActionAdded(action);
		}
	}

	/**
	 * Checks if there is an undoable action
	 * @return
	 */
	public boolean hasUndoAction() {
		return _top != null;
	}

	/**
	 * Checks if there is a redoable action
	 * @return
	 */
	public boolean hasRedoAction() {
		return _topNext != null;
	}

	/**
	 * Performs undo or does nothing if not available
	 */
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

	/**
	 * Performs redo or does nothing if not available
	 */
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
