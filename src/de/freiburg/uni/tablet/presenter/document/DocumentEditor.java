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

	private Document _baseDocument = null;
	
	private ServerDocument _document = null;
	private final DocumentHistory _history;
	private DocumentPage _currentPage = null;
	private DocumentPage _currentBackPage = null;
	
	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();

	public DocumentEditor() {
		_history = new DocumentHistory(this);
		//_currentPen = new SolidPen(config.getFloat("editor.defaultPen.thickness", 1f), config.getColor("editor.defaultPen.color", Color.black));
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
		_currentPage = page;
		if (_baseDocument != null) {
			int pageIndex = _document.getPageIndex(page);
			_currentBackPage = _baseDocument.getPageByIndex(pageIndex);
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
		// TODO: Check calling methods for result
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
		_currentPen = currentPen;
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
	 */
	public void setCurrentImageFile(File imageFile) {
		_currentImageFile = imageFile;
		_currentImage = new BitmapImage(0, imageFile, 0, 0, 0, 0);
	}
	
	/**
	 * 
	 * @param baseDocument
	 */
	public void setBaseDocument(final Document baseDocument) {
		fireChanging();
		_baseDocument = baseDocument;
		int length = _baseDocument.getPageCount();
		if (getCurrentPageIndex() >= length) {
			setCurrentPageByIndex(length - 1, true);
		}
		// Create all pages
		_document.getPageByIndex(length - 1, true);
	}

	/**
	 * @return
	 */
	public ServerDocument getDocument() {
		return _document;
	}

	/**
	 * @param document
	 */
	public void setDocument(final ServerDocument document) {
		fireChanging();
		final ServerDocument lastDocument = _document;
		_document = document;
		fireDocumentChanged(lastDocument);
		setCurrentPage(document.getPageByIndex(0, true));
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

	private void fireDocumentChanged(final Document lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.documentChanged(lastDocument);
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
		_currentPen = reader.readSerializableClass();
		_currentPage = reader.readObjectTable();
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
		writer.writeObjectTable(_baseDocument);
		writer.writeSerializableClass(_currentPen);
		writer.writeObjectTable(_currentPage);
	}

	@Override
	public long getId() {
		return 0;
	}
}
