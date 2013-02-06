package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public class DocumentEditorServer implements IDocumentEditor {
	private IClientDocument _document = null;
	private DocumentPage _currentPage = null;
	
	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();
	private DocumentListener _documentListener;
	
	private final DocumentHistory _history;

	public DocumentEditorServer() {
		_history = new DocumentHistory(this);
		_documentListener = new DocumentAdapter() {
			@Override
			public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				onDocumentPageRemoved(document, prevPage, page);
			}
		};
	}
	
	protected void onDocumentPageRemoved(final IDocument document,
			final DocumentPage prevPage, final DocumentPage page) {
		if (_currentPage == page) {
			_currentPage = null;
		}
	}

	@Override
	public DocumentPage getCurrentPage() {
		return _currentPage;
	}

	@Override
	public void setCurrentPage(final DocumentPage page) {
		fireChanging();
		final DocumentPage lastPage = _currentPage;
		if (_document.hasPage(page)) {
			_currentPage = page;
		} else {
			throw new IllegalArgumentException("Page not in document");
		}
		fireCurrentPageChanged(lastPage, null);
	}

	@Override
	public int getCurrentPageIndex() {
		if (_document == null) {
			return -1;
		}
		return _document.getPageIndex(_currentPage);
	}

	@Override
	public void setCurrentPageByIndex(final int index) {
		if (index == -1) {
			setCurrentPage(null);
		} else {
			setCurrentPage(_document.getPageByIndex(index));
		}
	}
	
	@Override
	public IClientDocument getDocument() {
		return _document;
	}

	@Override
	public void setDocument(final IClientDocument document) {
		fireChanging();
		if (_document != null) {
			_document.removeListener(_documentListener);
		}
		final IClientDocument lastDocument = _document;
		_document = document;
		
		final DocumentPage lastPage = _currentPage;
		if (_document != null) {
			_currentPage = document.getPageByIndex(0);
			_document.addListener(_documentListener);
		} else {
			_currentPage = null;
		}
		fireDocumentChanged(lastDocument);
		fireCurrentPageChanged(lastPage, null);
	}
	

	@Override
	public void addListener(final DocumentEditorListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(final DocumentEditorListener listener) {
		_listeners.remove(listener);
	}
	
	private void fireChanging() {
		for (final DocumentEditorListener listener : _listeners) {
			listener.changing();
		}
	}

	private void fireCurrentPageChanged(final DocumentPage lastCurrentPage, final DocumentPage lastCurrentBackPage) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.currentPageChanged(lastCurrentPage, lastCurrentBackPage);
		}
	}

	private void fireDocumentChanged(final IClientDocument lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.documentChanged(lastDocument);
		}
	}

	@Override
	public DocumentHistory getHistory() {
		return _history;
	}

	public DocumentEditorServer(final BinaryDeserializer reader) throws IOException {
		reader.resetState();
		_document = reader.readObjectTable();
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex);
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_document);
		writer.writeInt(getCurrentPageIndex());
	}
	
	@Override
	public void deserialize(final BinaryDeserializer reader) throws IOException {
		reader.resetState();
		setDocument((IEditableDocument) reader.readObjectTable());
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex);
	}

	@Override
	public long getId() {
		return 0;
	}
}
