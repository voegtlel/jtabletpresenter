package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class DocumentEditor implements IBinarySerializableId {
	private IPen _currentPen = null;
	private File _currentImageFile = null;
	private BitmapImage _currentImage = null;

	private IDocument _baseDocument = null;
	
	private IEditableDocument _document = null;
	private final DocumentHistory _history;
	private DocumentPage _currentPage = null;
	private DocumentPage _currentBackPage = null;
	
	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();

	public DocumentEditor() {
		_history = new DocumentHistory(this);
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
	 * Returns the current page.
	 * 
	 * @return
	 */
	public DocumentPage getCurrentBackPage() {
		return _currentBackPage;
	}

	/**
	 * Set the current page
	 * 
	 * @param page
	 */
	public void setCurrentPage(final DocumentPage page) {
		fireChanging();
		final DocumentPage lastPage = _currentPage;
		final DocumentPage lastBackPage = _currentBackPage;
		if (_document.hasPage(page)) {
			_currentPage = page;
			if (_baseDocument != null) {
				int pageIndex = _document.getPageIndex(page);
				_currentBackPage = _baseDocument.getPageByIndex(pageIndex);
			}
		} else if ((_baseDocument != null) && _baseDocument.hasPage(page)) {
			_currentBackPage = page;
			int pageIndex = _baseDocument.getPageIndex(page);
			_currentPage = _document.getPageByIndex(pageIndex, true);
		} else {
			throw new IllegalArgumentException("Page not in document");
		}
		fireCurrentPageChanged(lastPage, lastBackPage);
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
		if (_baseDocument != null) {
			return _baseDocument.getPageCount();
		}
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
		if (_baseDocument != null) {
			// Only verify here
			DocumentPage backPage = _baseDocument.getPageByIndex(index);
			if (backPage == null) {
				throw new IllegalStateException("Page index out of range");
			}
		}
		final DocumentPage page = _document.getPageByIndex(index,
				createIfNotExisting);
		if (page == null) {
			throw new IllegalStateException("Page index out of range");
		}
		setCurrentPage(page);
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
	 * 
	 * @param baseDocument
	 */
	public void setBaseDocument(final IDocument baseDocument) {
		fireChanging();
		final IDocument lastDocument = _baseDocument;
		_baseDocument = baseDocument;
		fireBaseDocumentChanged(lastDocument);
		boolean pageChanged = false;
		if (_baseDocument != null) {
			int length = _baseDocument.getPageCount();
			// Create all pages
			_document.getPageByIndex(length - 1, true);
			// Set page index
			if (getCurrentPageIndex() >= length) {
				setCurrentPageByIndex(length - 1, true);
				pageChanged = true;
			}
		}
		if (!pageChanged) {
			setCurrentPage(_currentPage);
		}
	}
	
	/**
	 * @return
	 */
	public IDocument getBaseDocument() {
		return _baseDocument;
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
		final IEditableDocument lastDocument = _document;
		_document = document;
		
		final DocumentPage lastPage = _currentPage;
		final DocumentPage lastBackPage = _currentBackPage;
		_currentPage = document.getPageByIndex(0, true);
		if (_baseDocument != null) {
			_currentBackPage = _baseDocument.getPageByIndex(0);
		}
		fireDocumentChanged(lastDocument);
		fireCurrentPageChanged(lastPage, lastBackPage);
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
	
	private void fireBaseDocumentChanged(final IDocument lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.baseDocumentChanged(lastDocument);
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

	public DocumentEditor(final BinaryDeserializer reader) throws IOException {
		_document = reader.readObjectTable();
		_baseDocument = reader.readObjectTable();
		_currentPen = reader.readSerializableClass();
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
		writer.writeObjectTable(_baseDocument);
		writer.writeSerializableClass(_currentPen);
		writer.writeInt(getCurrentPageIndex());
	}
	
	/**
	 * Override this by deserializing an editor
	 * @param reader
	 * @throws IOException
	 */
	public void deserialize(final BinaryDeserializer reader) throws IOException {
		setDocument((IEditableDocument) reader.readObjectTable());
		setBaseDocument((IEditableDocument) reader.readObjectTable());
		setCurrentPen((IPen) reader.readSerializableClass());
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
	}

	@Override
	public long getId() {
		return 0;
	}
}
