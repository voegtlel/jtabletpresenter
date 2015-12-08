package de.freiburg.uni.tablet.presenter.document.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public class DocumentClientServer implements IEditableDocument {
	private final IEditableDocument _document;
	private IClientDocument _backDocument;
	
	private final List<DocumentListener> _listeners = new LinkedList<DocumentListener>();
	
	private DocumentListener _documentListener = new DocumentListener() {
		@Override
		public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
				final DocumentPage page) {
			onDocumentPageRemoved(document, prevPage, page);
		}
		
		@Override
		public void pageInserted(final IClientDocument document,
				final DocumentPage prevPage, final DocumentPage page) {
			onDocumentPageInserted(document, prevPage, page);
		}
		
		@Override
		public void backgroundEntityChanged(final DocumentPage documentPage,
				final IEntity lastBackgroundEntity) {
			onDocumentBackgroundEntityChanged(documentPage, lastBackgroundEntity);
		}
		
		@Override
		public void renderableAdded(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableAdded(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableModified(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModified(renderable, page);
		}
		
		@Override
		public void renderableModifyEnd(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModifyEnd(renderable, page);
		}
		
		@Override
		public void renderableRemoved(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableRemoved(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableAdding(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableAdding(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableModifying(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModifying(renderable, page);
		}
		
		@Override
		public void renderableRemoving(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableRemoving(renderableAfter, renderable, page);
		}
	};
	private DocumentListener _backDocumentListener = new DocumentListener() {
		@Override
		public void pageRemoved(final IClientDocument document, final DocumentPage prevPage,
				final DocumentPage page) {
			onBackDocumentPageRemoved(document, prevPage, page);
		}
		
		@Override
		public void pageInserted(final IClientDocument document,
				final DocumentPage prevPage, final DocumentPage page) {
			onBackDocumentPageInserted(document, prevPage, page);
		}
		
		@Override
		public void backgroundEntityChanged(final DocumentPage documentPage,
				final IEntity lastBackgroundEntity) {
			onDocumentBackgroundEntityChanged(documentPage, lastBackgroundEntity);
		}
		
		@Override
		public void renderableAdded(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableAdded(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableModified(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModified(renderable, page);
		}
		
		@Override
		public void renderableModifyEnd(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModifyEnd(renderable, page);
		}
		
		@Override
		public void renderableRemoved(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableRemoved(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableAdding(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableAdding(renderableAfter, renderable, page);
		}
		
		@Override
		public void renderableModifying(final IRenderable renderable,
				final DocumentPage page) {
			onDocumentRenderableModifying(renderable, page);
		}
		
		@Override
		public void renderableRemoving(final IRenderable renderableAfter,
				final IRenderable renderable, final DocumentPage page) {
			onDocumentRenderableRemoving(renderableAfter, renderable, page);
		}
	};
	
	private HashMap<Long, DocumentPage> _pageTranslator = new HashMap<Long, DocumentPage>();
	private HashMap<Long, DocumentPage> _backPageTranslator = new HashMap<Long, DocumentPage>();

	public DocumentClientServer(final IEditableDocument document, final IClientDocument backDocument) {
		_document = document;
		_backDocument = backDocument;
		
		document.addListener(_documentListener);
		if (backDocument != null) {
			backDocument.addListener(_backDocumentListener);
		}
	}

	protected void onDocumentPageRemoved(final IClientDocument document,
			final DocumentPage prevPage, final DocumentPage page) {
		firePageRemoved(document, prevPage, page);
	}

	protected void onDocumentPageInserted(final IClientDocument document,
			final DocumentPage prevPage, final DocumentPage page) {
		firePageInserted(document, prevPage, page);
	}

	protected void onDocumentBackgroundEntityChanged(final DocumentPage documentPage,
			final IEntity lastBackgroundEntity) {
		fireBackgroundEntityChanged(documentPage, lastBackgroundEntity);
	}

	protected void onDocumentRenderableAdded(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
		fireRenderableAdded(renderableAfter, renderable, page);
	}

	protected void onDocumentRenderableModified(final IRenderable renderable,
			final DocumentPage page) {
		fireRenderableModified(renderable, page);
	}

	protected void onDocumentRenderableModifyEnd(final IRenderable renderable,
			final DocumentPage page) {
		fireRenderableModifyEnd(renderable, page);
	}

	protected void onDocumentRenderableRemoved(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
		fireRenderableRemoved(renderableAfter, renderable, page);
	}
	
	protected void onDocumentRenderableAdding(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
		fireRenderableAdding(renderableAfter, renderable, page);
	}

	protected void onDocumentRenderableModifying(final IRenderable renderable,
			final DocumentPage page) {
		fireRenderableModifying(renderable, page);
	}

	protected void onDocumentRenderableRemoving(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
		fireRenderableRemoving(renderableAfter, renderable, page);
	}
	
	protected void onBackDocumentPageRemoved(final IClientDocument backDocument,
			final DocumentPage prevBackPage, final DocumentPage backPage) {
		final DocumentPage page = _backPageTranslator.get(backPage.getId());
		_document.removePage(page);
		_pageTranslator.remove(page.getId());
		_backPageTranslator.remove(backPage.getId());
	}

	protected void onBackDocumentPageInserted(final IClientDocument backDocument,
			final DocumentPage prevBackPage, final DocumentPage backPage) {
		final DocumentPage prevPage = _backPageTranslator.get(prevBackPage.getId());
		final DocumentPage page = _document.insertPage(prevPage);
		_pageTranslator.put(page.getId(), backPage);
		_backPageTranslator.put(backPage.getId(), page);
	}

	@Override
	public DocumentPage insertPage(final DocumentPage afterPage, final DocumentPage page) {
		return _document.insertPage(afterPage, page);
	}

	@Override
	public void removePage(final DocumentPage page) {
		if (_pageTranslator.containsKey(page)) {
			throw new IllegalStateException("Can't remove page that has a back page");
		}
		_document.removePage(page);
	}

	@Override
	public DocumentPage getPageByIndex(final int index) {
		return _document.getPageByIndex(index);
	}

	@Override
	public int getPageCount() {
		return _document.getPageCount();
	}

	@Override
	public boolean hasPage(final DocumentPage page) {
		return _document.hasPage(page);
	}

	@Override
	public DocumentPage getPageById(final long id) {
		return _document.getPageById(id);
	}

	@Override
	public int getPageIndex(final DocumentPage page) {
		return _document.getPageIndex(page);
	}
	
	public void setBackDocument(final IClientDocument backDocument) {
		if (_backDocument != null) {
			_backDocument.removeListener(_backDocumentListener);
		}
		final IClientDocument lastBackDocument = _backDocument;
		_backDocument = backDocument;
		if (_backDocument == null) {
			_pageTranslator.clear();
			_backPageTranslator.clear();
		} else {
			_backDocument.addListener(_backDocumentListener);
			if ((lastBackDocument != null) && _backDocument.getUuid().equals(lastBackDocument.getUuid())) {
				// If it is the same base document -> update index
				// Delete removed pages from translator and document
				for (Iterator<DocumentPage> iterator = _pageTranslator.values().iterator(); iterator.hasNext();) {
					final DocumentPage lastBackPage = iterator.next();
					if (_backDocument.getPageById(lastBackPage.getId()) == null) {
						DocumentPage lastPage = _backPageTranslator.get(lastBackPage.getId());
						_document.removePage(lastPage);
						iterator.remove();
						_backPageTranslator.remove(lastBackPage.getId());
						System.out.println("Remove back page " + lastBackPage.getId());
					}
				}
				// Update/Insert existing pages
				DocumentPage lastPage = null;
				for (DocumentPage backPage : _backDocument.getPages()) {
					final DocumentPage page = _backPageTranslator.get(backPage.getId());
					if (page != null) {
						// Update page
						_pageTranslator.put(page.getId(), backPage);
						lastPage = page;
						System.out.println("Update back page " + backPage.getId() + " -> " + lastPage.getId());
					} else {
						// Insert page
						lastPage = _document.insertPage(lastPage);
						_pageTranslator.put(lastPage.getId(), backPage);
						_backPageTranslator.put(backPage.getId(), lastPage);
						System.out.println("Insert back page " + backPage.getId() + " -> " + lastPage.getId());
					}
				}
			} else {
				_pageTranslator.clear();
				_backPageTranslator.clear();
				// Insert all pages at front
				DocumentPage lastPage = null;
				for (DocumentPage backPage : _backDocument.getPages()) {
					lastPage = _document.insertPage(lastPage);
					System.out.println("Insert back page " + backPage.getId() + " -> " + lastPage.getId());
					_pageTranslator.put(lastPage.getId(), backPage);
					_backPageTranslator.put(backPage.getId(), lastPage);
				}
			}
		}
	}
	
	public IClientDocument getBackDocument() {
		return _backDocument;
	}
	
	public IEditableDocument getDocument() {
		return _document;
	}
	
	/**
	 * Returns the back page for the given front page or null
	 * @param page
	 * @return
	 */
	public DocumentPage getBackPage(final DocumentPage page) {
		return _pageTranslator.get(page.getId());
	}
	
	/**
	 * Returns the associated front page for the given page (might be the page itself) or null if not in front document and not in back pages
	 * @param page
	 * @return
	 */
	public DocumentPage getPage(final DocumentPage backPage) {
		return _backPageTranslator.get(backPage.getId());
	}
	
	/**
	 * Returns the associated front page for the given page (might be the page itself) or throws an exception
	 * @param page
	 * @return
	 */
	public DocumentPage getPageSafe(final DocumentPage page) {
		DocumentPage result = _backPageTranslator.get(page);
		if (result == null) {
			if (_document.hasPage(page)) {
				return page;
			}
			throw new IllegalArgumentException("Page not in document or in back document");
		}
		return result;
	}

	@Override
	public long nextId() {
		throw new IllegalStateException("Can't get next id on delegate document");
	}

	@Override
	public void deserializeId(final long id) {
		throw new IllegalStateException("Can't deserialize id on delegate document");
	}
	
	protected void firePageInserted(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageInserted(document, prevPage, page);
		}
	}

	protected void firePageRemoved(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageRemoved(document, prevPage, page);
		}
	}

	
	protected void fireRenderableAdded(final IRenderable afterRenderable, final IRenderable renderable,
			final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdded(afterRenderable, renderable, page);
		}
	}
	
	protected void fireRenderableAdding(final IRenderable afterRenderable, final IRenderable renderable,
			final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdding(afterRenderable, renderable, page);
		}
	}
	
	protected void fireRenderableRemoved(final IRenderable afterRenderable,
			final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoved(afterRenderable, renderable, page);
		}
	}
	
	protected void fireRenderableRemoving(final IRenderable afterRenderable,
			final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoving(afterRenderable, renderable, page);
		}
	}
	
	protected void fireRenderableModified(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModified(renderable, page);
		}
	}
	
	protected void fireRenderableModifying(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModifying(renderable, page);
		}
	}
	
	protected void fireRenderableModifyEnd(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModifyEnd(renderable, page);
		}
	}
	
	protected void fireBackgroundEntityChanged(final DocumentPage documentPage,
			final IEntity lastBackgroundEntity) {
		for (final DocumentListener listener : _listeners) {
			listener.backgroundEntityChanged(documentPage, lastBackgroundEntity);
		}
	}

	@Override
	public void addListener(final DocumentListener listener) {
		_listeners.add(listener);
	}
	
	@Override
	public void removeListener(final DocumentListener listener) {
		_listeners.remove(listener);
	}
	
	/**
	 * Adds a listener only for the inner front document
	 * @param listener
	 */
	public void addDocumentListener(final DocumentListener listener) {
		_document.addListener(listener);
	}
	
	/**
	 * Removes a listener from the inner front document
	 * @param listener
	 */
	public void removeDocumentListener(final DocumentListener listener) {
		_document.addListener(listener);
	}

	@Override
	public DocumentPage getNextPage(final DocumentPage page) {
		return _document.getNextPage(page);
	}

	@Override
	public DocumentPage getPreviousPage(final DocumentPage page) {
		return _document.getPreviousPage(page);
	}

	@Override
	public UUID getUuid() {
		return _document.getUuid();
	}

	@Override
	public IEntity getParent() {
		return _document.getParent();
	}

	@Override
	public long getId() {
		return _document.getId();
	}
	
	public DocumentClientServer(final BinaryDeserializer reader) throws IOException {
		reader.resetState();
		_backDocument = reader.readObjectTable();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final Long id = reader.readLong();
			final DocumentPage page = reader.readObjectTable();
			_pageTranslator.put(id, page);
		}
		reader.resetState();
		_document = reader.readObjectTable();
		
		for (Entry<Long, DocumentPage> translatorEntry : _pageTranslator.entrySet()) {
			final DocumentPage page = _document.getPageById(translatorEntry.getKey());
			_backPageTranslator.put(translatorEntry.getValue().getId(), page);
		}
		
		_document.addListener(_documentListener);
		if (_backDocument != null) {
			_backDocument.addListener(_backDocumentListener);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_backDocument);
		writer.writeInt(_pageTranslator.size());
		for (Entry<Long, DocumentPage> translatorEntry : _pageTranslator.entrySet()) {
			writer.writeLong(translatorEntry.getKey());
			writer.writeObjectTable(translatorEntry.getValue());
		}
		writer.resetState();
		writer.writeObjectTable(_document);
	}

	@Override
	public void setBackPages(final IBackDocument document, final int documentMode) {
		if (_backDocument != null) {
			throw new IllegalStateException("Can't set background document on server sync document");
		}
		_document.setBackPages(document, documentMode);
	}

	@Override
	public DocumentPage getPageByIndex(final int index, final boolean createIfNotExisting) {
		return _document.getPageByIndex(index, createIfNotExisting);
	}

	@Override
	public DocumentPage addPage() {
		return _document.addPage();
	}

	@Override
	public DocumentPage insertPage(final DocumentPage afterPage) {
		return _document.insertPage(afterPage);
	}

	@Override
	public void clear() {
		if (_backDocument != null) {
			throw new IllegalStateException("Can't clear server sync document");
		}
		_document.clear();
	}

	@Override
	public IEditableDocument clone(final int docId) {
		throw new IllegalStateException("Can't clone server client document");
	}

	@Override
	public DocumentPage clonePage(final DocumentPage page) {
		return _document.clonePage(page);
	}

	@Override
	public DocumentPage getNextPage(final DocumentPage page, final boolean createIfNotExists) {
		return _document.getNextPage(page, createIfNotExists);
	}
	
	@Override
	public Iterable<DocumentPage> getPages() {
		return _document.getPages();
	}
	
	@Override
	public String toString() {
		return String.format("DocumentServerClient %X (%X)", getId(), System.identityHashCode(this));
	}
}
