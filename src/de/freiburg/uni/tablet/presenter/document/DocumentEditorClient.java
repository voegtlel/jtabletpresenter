package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class DocumentEditorClient implements IDocumentEditorClient {
	private IPen _currentPen = null;
	private File _currentImageFile = null;
	private BitmapImage _currentImage = null;
	
	private IEditableDocument _document = null;
	private final DocumentHistory _history;
	private DocumentPage _currentPage = null;
	
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
	
	/**
	 * Returns the current page.
	 * 
	 * @return
	 */
	public DocumentPage getCurrentPage() {
		return _currentPage;
	}
	
	/**
	 * Set the current page
	 * 
	 * @param page
	 */
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
	
	/**
	 * Event
	 * @param document
	 * @param prevPage
	 * @param page
	 */
	protected void onDocumentPageRemoved(final IDocument document,
			final DocumentPage prevPage, final DocumentPage page) {
		// TODO: Recreate front page or insert event to prevent deletion
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

	/**
	 * Returns the current page index.
	 * 
	 * @return
	 */
	public int getCurrentPageIndex() {
		return _document.getPageIndex(_currentPage);
	}
	
	/**
	 * Gets the maximum number of pages available
	 * @return Integer.MAX_VALUE or number of maximum pages
	 */
	public int getMaxPageCount() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Set the current page
	 * 
	 * @param index
	 * @param createIfNotExisting
	 */
	public void setCurrentPageByIndex(final int index,
			final boolean createIfNotExisting) {
		final DocumentPage page = _document.getPageByIndex(index,
				createIfNotExisting);
		if (page == null) {
			throw new IllegalStateException("Page index out of range");
		}
		setCurrentPage(page);
	}
	
	@Override
	public void setCurrentPageByIndex(final int index) {
		setCurrentPageByIndex(index, false);
	}

	/**
	 * @return
	 */
	public IPen getCurrentPen() {
		return _currentPen;
	}

	/**
	 * @param currentPen
	 */
	public void setCurrentPen(final IPen currentPen) {
		fireChanging();
		final IPen lastPen = _currentPen;
		_currentPen = currentPen;
		fireCurrentPenChanged(lastPen);
	}
	
	/**
	 * @return
	 */
	public File getCurrentImageFile() {
		return _currentImageFile;
	}
	
	/**
	 * @return
	 */
	public BitmapImage getCurrentImage() {
		return _currentImage;
	}
	
	/**
	 * @param imageFile
	 * @throws IOException 
	 */
	public void setCurrentImageFile(File imageFile) throws IOException {
		fireChanging();
		_currentImageFile = imageFile;
		_currentImage = new BitmapImage(_currentPage, imageFile, 0, 0, 0, 0);
	}
	
	/**
	 * @return
	 */
	public IEditableDocument getDocument() {
		return _document;
	}

	/**
	 * @param document
	 */
	public void setDocument(final IEditableDocument document) {
		fireChanging();
		if (_document != null) {
			_document.removeListener(_documentListener);
		}
		final IEditableDocument lastDocument = _document;
		_document = document;
		
		final DocumentPage lastPage = _currentPage;
		if (_document != null) {
			_currentPage = document.getPageByIndex(0, true);
			_document.addListener(_documentListener);
		} else {
			_currentPage = null;
		}
		fireDocumentChanged(lastDocument);
		fireCurrentPageChanged(lastPage, null);
	}
	
	@Override
	public void setDocument(final IClientDocument document) {
		setDocument((IEditableDocument)document);
	}

	public void addListener(final DocumentEditorListener listener) {
		_listeners.add(listener);
	}

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
	
	private void fireCurrentPenChanged(final IPen lastPen) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.currentPenChanged(lastPen);
		}
	}

	/**
	 * @return
	 */
	public DocumentHistory getHistory() {
		return _history;
	}
	
	@Override
	public DocumentPage getCurrentBackPage() {
		return null;
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
	
	/**
	 * Override this by deserializing an editor
	 * @param reader
	 * @throws IOException
	 */
	public void deserialize(final BinaryDeserializer reader) throws IOException {
		reader.resetState();
		setDocument((IEditableDocument) reader.readObjectTable());
		setCurrentPen((IPen) reader.readSerializableClass());
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
	}

	@Override
	public long getId() {
		return 0;
	}
}
