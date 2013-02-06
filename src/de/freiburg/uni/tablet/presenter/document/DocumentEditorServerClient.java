package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class DocumentEditorServerClient implements IDocumentEditorClient {
	private IPen _currentPen = null;
	private File _currentImageFile = null;
	private BitmapImage _currentImage = null;

	private IDocument _baseDocument = null;
	
	private IEditableDocument _document = null;
	private final DocumentHistory _history;
	private DocumentPage _currentPage = null;
	private DocumentPage _currentBackPage = null;
	
	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();
	private DocumentListener _documentListener;
	private DocumentListener _baseDocumentListener;
	
	private HashMap<Long, Long> _pageTranslator = new HashMap<Long, Long>();
	private HashMap<Long, Long> _basePageTranslator = new HashMap<Long, Long>();


	public DocumentEditorServerClient() {
		_history = new DocumentHistory(this);
		_documentListener = new DocumentAdapter() {
			@Override
			public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				onDocumentPageRemoved(document, prevPage, page);
			}
		};
		
		_baseDocumentListener = new DocumentAdapter() {
			@Override
			public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
					final DocumentPage page) {
				onBaseDocumentPageRemoved(document, prevPage, page);
			}
			
			@Override
			public void pageInserted(IClientDocument document,
					DocumentPage prevPage, DocumentPage page) {
				onBaseDocumentPageInserted(document, prevPage, page);
			}
		};
	}
	
	private DocumentPage pageToBackPage(final DocumentPage page) {
		final Long result = _pageTranslator.get(page.getId());
		if (result != null) {
			return _document.getPageById(result);
		}
		return null;
	}
	
	private DocumentPage backPageToPage(final DocumentPage backPage) {
		final Long result = _basePageTranslator.get(backPage.getId());
		if (result != null) {
			return _baseDocument.getPageById(result);
		}
		throw new IllegalArgumentException("Missing page for backPage");
	}

	protected void onBaseDocumentPageRemoved(final IClientDocument document,
			final DocumentPage prevBackPage, final DocumentPage backPage) {
		// TODO: Here might be a bug on page deletion and thread sync
		final DocumentPage page = backPageToPage(backPage);
		_document.removePage(page);
	}
	
	protected void onBaseDocumentPageInserted(final IClientDocument document,
			final DocumentPage prevBackPage, final DocumentPage backPage) {
		// TODO: Here might be a bug on page deletion and thread sync
		final DocumentPage prevPage = backPageToPage(prevBackPage);
		final DocumentPage page = _document.insertPage(prevPage);
		_pageTranslator.put(page.getId(), backPage.getId());
		_basePageTranslator.put(backPage.getId(), page.getId());
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
		if (_document.hasPage(page)) {
			_currentPage = page;
			_currentBackPage = pageToBackPage(_currentPage);
		} else if ((_baseDocument != null) && _baseDocument.hasPage(page)) {
			_currentBackPage = page;
			_currentPage = backPageToPage(_currentBackPage);
		} else {
			throw new IllegalArgumentException("Page not in document");
		}
		fireCurrentPageChanged(lastPage, lastBackPage);
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
	@Override
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
		if (_baseDocument != null) {
			_baseDocument.removeListener(_baseDocumentListener);
		}
		final IDocument lastDocument = _baseDocument;
		_baseDocument = baseDocument;
		if (_baseDocument != null) {
			_baseDocument.addListener(_baseDocumentListener);
		}
		fireBaseDocumentChanged(lastDocument);
		if (_baseDocument != null) {
			// Create all pages for back document and insert them (merge)
			DocumentPage currentFrontPage = null;
			DocumentPage prevPage = null;
			for (DocumentPage page = _baseDocument.getPageByIndex(0); page != null; page = _baseDocument.getNextPage(page)) {
				if (!_basePageTranslator.containsKey(page.getId())) {
					// Find previous front page for previous back page and insert after this page
					final Long prevPageId = _basePageTranslator.get(prevPage);
					if (prevPageId != null && ((currentFrontPage == null) || (currentFrontPage.getId() != prevPageId))) {
						currentFrontPage = _document.getPageById(prevPageId);
					}
					// Insert new front page
					currentFrontPage = _document.insertPage(currentFrontPage);
					_basePageTranslator.put(page.getId(), currentFrontPage.getId());
					_pageTranslator.put(currentFrontPage.getId(), page.getId());
				}
				prevPage = page;
			}
			// TODO: Delete unused back pages
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
		if (_baseDocument != null) {
			throw new IllegalStateException("Can't set document if having base document");
		}
		fireChanging();
		if (_document != null) {
			_document.removeListener(_documentListener);
		}
		final IEditableDocument lastDocument = _document;
		_document = document;
		
		final DocumentPage lastPage = _currentPage;
		final DocumentPage lastBackPage = _currentBackPage;
		if (_document != null) {
			_currentPage = document.getPageByIndex(0, true);
			if (_baseDocument != null) {
				_currentBackPage = pageToBackPage(_currentPage);
			}
			_document.addListener(_documentListener);
		} else {
			_currentPage = null;
			_currentBackPage = null;
		}
		fireDocumentChanged(lastDocument);
		fireCurrentPageChanged(lastPage, lastBackPage);
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

	public DocumentEditorServerClient(final BinaryDeserializer reader) throws IOException {
		reader.resetState();
		_document = reader.readObjectTable();
		reader.resetState();
		_baseDocument = reader.readObjectTable();
		reader.resetState();
		_currentPen = reader.readSerializableClass();
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
		writer.resetState();
		writer.writeObjectTable(_baseDocument);
		writer.resetState();
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
		reader.resetState();
		setBaseDocument((IEditableDocument) reader.readObjectTable());
		reader.resetState();
		setCurrentPen((IPen) reader.readSerializableClass());
		int pageIndex = reader.readInt();
		setCurrentPageByIndex(pageIndex, false);
	}

	@Override
	public long getId() {
		return 0;
	}
}
