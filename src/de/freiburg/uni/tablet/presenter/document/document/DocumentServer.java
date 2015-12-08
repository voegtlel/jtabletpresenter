package de.freiburg.uni.tablet.presenter.document.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

public class DocumentServer extends Document implements IEditableDocument {
	/**
	 * Create a new server document.
	 */
	public DocumentServer(final int docId) {
		super(docId);
		_pages.addFirst(new DocumentPage(this));
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected DocumentServer(final int docId, final DocumentServer base) {
		super(docId, base);
	}
	
	@Override
	public void setBackPages(final IBackDocument document, final int pdfMode) {
		if (document != null) {
			if (pdfMode == DOCUMENT_MODE_REINDEX) {
				// Ensure the pages exist (enough space)
				getPageByIndex(document.getPageCount() - 1, true);
				// Iterate through pdf pages
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				for (IBackDocumentPage page : document.getPages()) {
					// Set indices
					docPage.getData().setBackgroundEntity(page.createEntity(docPage.getData()));
					docPage = docPage.getNext();
				}
				while (docPage != null) {
					docPage.getData().setBackgroundEntity(null);
					docPage = docPage.getNext();
				}
			} else if (pdfMode == DOCUMENT_MODE_KEEP_INDEX) {
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				int lastPdfIndex = 0;
				while (docPage.getNext() != null) {
					IEntity current = docPage.getData().getBackgroundEntity();
					if (current != null && current instanceof IBackDocumentPageEntity && ((IBackDocumentPageEntity)current).getPageIndex() > lastPdfIndex) {
						lastPdfIndex = ((IBackDocumentPageEntity)current).getPageIndex();
					}
					docPage = docPage.getNext();
				}
				if (lastPdfIndex < document.getPageCount() - 1) {
					for (IBackDocumentPage locPage : document.getPagesAt(lastPdfIndex)) {
						final DocumentPage newPage = this.addPage();
						newPage.setBackgroundEntity(locPage.createEntity(newPage));
					}
				}
			} else if (pdfMode == DOCUMENT_MODE_APPEND) {
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				while (docPage != null) {
					docPage.getData().setBackgroundEntity(null);
					docPage = docPage.getNext();
				}
				for (IBackDocumentPage locPage : document.getPages()) {
					final DocumentPage newPage = this.addPage();
					newPage.setBackgroundEntity(locPage.createEntity(newPage));
				}
			} else if (pdfMode == DOCUMENT_MODE_CLEAR) {
				// Clear and create new
				clear();
				for (IBackDocumentPage locPage : document.getPages()) {
					DocumentPage page = this.getPageByIndex(locPage.getPageIndex(), true);
					page.setBackgroundEntity(locPage.createEntity(page));
				}
			}
		} else {
			if (pdfMode == DOCUMENT_MODE_CLEAR) {
				// Clear and reset whole document
				clear();
			} else if (pdfMode == DOCUMENT_MODE_REINDEX) {
				// Iterate through pdf pages and clear pdf index
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				while (docPage != null) {
					docPage.getData().setBackgroundEntity(null);
					docPage = docPage.getNext();
				}
			} else {
				throw new IllegalStateException("Invalid pdfMode for null document");
			}
		}
	}

	@Override
	public DocumentPage getPageByIndex(final int index,
			final boolean createIfNotExisting) {
		// Search manually for performance
		int i = index;
		for (LinkedElement<DocumentPage> j = _pages.getFirst(); j != null; j = j
				.getNext()) {
			if (i == 0) {
				return j.getData();
			}
			i--;
		}
		// Add the new pages
		DocumentPage page = null;
		if (createIfNotExisting) {
			while (i >= 0) {
				page = addPage();
				i--;
			}
		}
		return page;
	}
	
	private DocumentPage addPage(final DocumentPage page) {
		if (page.getParent() != this) {
			return addPage(page.clone(this));
		}
		final LinkedElement<DocumentPage> lastPage = _pages.getLast();
		_pages.addLast(page);
		firePageInserted(this, (lastPage == null?null:lastPage.getData()), page);
		return page;
	}
	
	@Override
	public DocumentPage addPage() {
		return addPage(new DocumentPage(this));
	}

	@Override
	public DocumentPage insertPage(final DocumentPage afterPage,
			final DocumentPage page) {
		if (page.getParent() != this) {
			return insertPage(afterPage, page.clone(this));
		}
		if (afterPage != null && afterPage.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, page);
			firePageInserted(this, element.getData(), page);
		} else {
			_pages.addFirst(page);
			firePageInserted(this, null, page);
		}
		return page;
	}
	
	@Override
	public DocumentPage insertPage(final DocumentPage afterPage) {
		return insertPage(afterPage, new DocumentPage(this));
	}
	
	@Override
	public DocumentPage getNextPage(final DocumentPage page, final boolean createIfNotExists) {
		DocumentPage nextPage = getNextPage(page);
		if (nextPage == null) {
			if (createIfNotExists) {
				return addPage();
			} else {
				throw new IllegalStateException("Page has no next page");
			}
		}
		return nextPage;
	}

	@Override
	public void removePage(final DocumentPage page) {
		if (page.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		final LinkedElement<DocumentPage> element = _pages
				.getElementByInstance(page);
		if (element == null) {
			throw new IllegalArgumentException("afterPage not in document");
		}
		if (_pages.hasOne()) {
			addPage();
		}
		final LinkedElement<DocumentPage> prevPage = element.getPrevious();
		_pages.remove(element);
		firePageRemoved(this, (prevPage == null?null:prevPage.getData()), element.getData());
	}
	
	@Override
	public void clear() {
		addPage();
		while (!_pages.hasOne()) {
			final LinkedElement<DocumentPage> element = _pages.getFirst();
			_pages.removeFirst();
			firePageRemoved(this, null, element.getData());
		}
	}
	
	@Override
	public DocumentPage clonePage(final DocumentPage page) {
		return page.clone(this);
	}
	
	public DocumentServer(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	@Override
	public IEditableDocument clone(final int docId) {
		return new DocumentServer(docId, this);
	}
}
