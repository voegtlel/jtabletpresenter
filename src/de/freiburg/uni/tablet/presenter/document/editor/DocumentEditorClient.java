package de.freiburg.uni.tablet.presenter.document.editor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.document.DocumentClientServer;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class DocumentEditorClient implements IDocumentEditorClient {
	private IPen _currentPen = null;
	private File _currentImageFile = null;
	private BitmapImage _currentImage = null;

	private DocumentClientServer _document = null;
	private final DocumentHistory _history;
	private DocumentPage _currentPage = null;
	private DocumentPage _currentBackPage = null;
	
	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();
	private DocumentListener _documentListener;

	public DocumentEditorClient() {
		_history = new DocumentHistory(this);
		_documentListener = new DocumentAdapter() {
			@Override
			public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				onDocumentPageRemoved(document, prevPage, page);
			}
		};
	}

	@Override
	public DocumentPage getCurrentPage() {
		return _currentPage;
	}
	
	@Override
	public DocumentPage getCurrentBackPage() {
		return _currentBackPage;
	}

	@Override
	public void setCurrentPage(final DocumentPage page) {
		fireChanging();
		final DocumentPage lastPage = _currentPage;
		final DocumentPage lastBackPage = _currentBackPage;
		if (page == null) {
			_currentPage = null;
			_currentBackPage = null;
		} else {
			if (_document.hasPage(page)) {
				_currentPage = page;
				_currentBackPage = _document.getBackPage(page);
			} else if ((_document.getBackDocument() != null) && _document.getBackDocument().hasPage(page)) {
				_currentBackPage = page;
				_currentPage = _document.getPage(page);
				if (_currentPage == null) {
					throw new IllegalStateException("BackPage " + page.getId() + " has no front page");
				}
			} else {
				throw new IllegalArgumentException("Page not in document");
			}
		}
		fireCurrentPageChanged(lastPage, lastBackPage);
	}
	
	/**
	 * Event. Updates the current page
	 * @param document
	 * @param prevPage
	 * @param page
	 */
	protected void onDocumentPageRemoved(final IDocument document,
			final DocumentPage prevPage, final DocumentPage page) {
		if (_currentPage == page) {
			if (prevPage != null) {
				final DocumentPage nextPage = document.getNextPage(prevPage);
				if (nextPage != null) {
					setCurrentPage(nextPage);
				} else {
					setCurrentPage(prevPage);
				}
			} else {
				setCurrentPage(_document.getPageByIndex(0));
			}
		}
	}

	@Override
	public int getCurrentPageIndex() {
		if (_document == null) {
			return -1;
		}
		return _document.getPageIndex(_currentPage);
	}
	
	@Override
	public void setCurrentPageByIndex(final int index,
			final boolean createIfNotExisting) {
		if (index == -1) {
			setCurrentPage(null);
		} else if (_document != null) {
			final DocumentPage page = _document.getPageByIndex(index,
					createIfNotExisting);
			if (page == null) {
				throw new IllegalStateException("Page index out of range");
			}
			setCurrentPage(page);
		}
	}
	
	@Override
	public void setCurrentPageByIndex(final int index) {
		setCurrentPageByIndex(index, false);
	}
	
	@Override
	public boolean isCurrentPage(final DocumentPage page) {
		return (_currentPage == page) || (_currentBackPage == page);
	}

	/**
	 * @return
	 */
	@Override
	public IPen getCurrentPen() {
		return _currentPen;
	}

	@Override
	public void setCurrentPen(final IPen currentPen) {
		fireChanging();
		final IPen lastPen = _currentPen;
		_currentPen = currentPen;
		fireCurrentPenChanged(lastPen);
	}
	
	@Override
	public File getCurrentImageFile() {
		return _currentImageFile;
	}
	
	@Override
	public BitmapImage getCurrentImage() {
		return _currentImage;
	}
	
	@Override
	public void setCurrentImageFile(final File imageFile) throws IOException {
		fireChanging();
		_currentImageFile = imageFile;
		_currentImage = new BitmapImage(_currentPage, imageFile, 0, 0, 0, 0);
	}
	
	@Override
	public void setBackDocument(final IClientDocument backDocument) {
		fireChanging();
		final IClientDocument lastBackDocument = _document.getBackDocument();
		_document.setBackDocument(backDocument);
		fireBackDocumentChanged(lastBackDocument);
	}
	
	@Override
	public IClientDocument getBackDocument() {
		return _document.getBackDocument();
	}

	@Override
	public IEditableDocument getFrontDocument() {
		return _document.getDocument();
	}
	
	@Override
	public IEditableDocument getDocument() {
		return _document;
	}

	/**
	 * @param document
	 */
	public void setDocument(final IEditableDocument document) {
		if ((_document != null) && _document.getBackDocument() != null) {
			throw new IllegalStateException("Can't set document if having back document");
		}
		fireChanging();
		setCurrentPage(null);
		if (_document != null) {
			_document.removeListener(_documentListener);
		}
		final IEditableDocument lastDocument = _document;
		if (document instanceof DocumentClientServer) {
			_document = (DocumentClientServer) document;
		} else {
			_document = new DocumentClientServer(document, null);
		}
		_currentPage = null;
		_currentBackPage = null;
		if (_document != null) {
			_document.addListener(_documentListener);
		}
		fireDocumentChanged(lastDocument);
		setCurrentPageByIndex(0);
	}
	
	@Override
	public void setDocument(final IClientDocument document) {
		setDocument((IEditableDocument)document);
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

	private void fireDocumentChanged(final IEditableDocument lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.documentChanged(lastDocument);
		}
	}
	
	private void fireBackDocumentChanged(final IDocument lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.backDocumentChanged(lastDocument);
		}
	}
	
	private void fireCurrentPenChanged(final IPen lastPen) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.currentPenChanged(lastPen);
		}
	}

	@Override
	public DocumentHistory getHistory() {
		return _history;
	}

	public DocumentEditorClient(final BinaryDeserializer reader) throws IOException {
		_document = reader.readObjectTable();
		_currentPen = reader.readSerializableClass();
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
		writer.writeSerializableClass(_currentPen);
		writer.writeInt(getCurrentPageIndex());
	}
	
	@Override
	public void deserialize(final BinaryDeserializer reader) throws IOException {
		setDocument((IEditableDocument) reader.readObjectTable());
		setCurrentPen((IPen) reader.readSerializableClass());
		setCurrentPageByIndex(reader.readInt());
	}

	@Override
	public long getId() {
		return 0;
	}
}
