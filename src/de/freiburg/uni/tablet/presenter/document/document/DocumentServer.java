package de.freiburg.uni.tablet.presenter.document.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;

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
	public void setPdfPages(final PdfSerializable document, final int pdfMode) {
		if (document != null) {
			if (pdfMode == PDF_MODE_REINDEX) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Ensure the pages exist (enough space)
				getPageByIndex(pageTree.getCount() - 1, true);
				// Iterate through pdf pages
				PDPage pdfPage = pageTree.getFirstPage();
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				while (pdfPage != null) {
					// Set indices
					docPage.getData().setBackgroundEntity(new PdfPageSerializable(docPage.getData(), document, pdfPage, document.tryGetPage(pdfPage.getNodeIndex() + 1)));
					pdfPage = pdfPage.getNextPage();
					docPage = docPage.getNext();
				}
				while (docPage != null) {
					docPage.getData().setBackgroundEntity(null);
					docPage = docPage.getNext();
				}
			} else if (pdfMode == PDF_MODE_KEEP_INDEX) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				int lastPdfIndex = 0;
				while (docPage.getNext() != null) {
					IEntity current = docPage.getData().getBackgroundEntity();
					if (current != null && current instanceof PdfPageSerializable && ((PdfPageSerializable)current).getPage().getNodeIndex() > lastPdfIndex) {
						lastPdfIndex = ((PdfPageSerializable)current).getPage().getNodeIndex();
					}
					docPage = docPage.getNext();
				}
				if (lastPdfIndex < pageTree.getCount() - 1) {
					PDPage pdfPage = pageTree.getPageAt(lastPdfIndex);
					while (pdfPage != null) {
						final DocumentPage newPage = this.addPage();
						newPage.setBackgroundEntity(new PdfPageSerializable(newPage, document, pdfPage, document.tryGetPage(lastPdfIndex + 1)));
						pdfPage = pdfPage.getNextPage();
					}
				}
			} else if (pdfMode == PDF_MODE_APPEND) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				while (docPage != null) {
					docPage.getData().setBackgroundEntity(null);
					docPage = docPage.getNext();
				}
				PDPage pdfPage = pageTree.getFirstPage();
				while (pdfPage != null) {
					final DocumentPage newPage = this.addPage();
					newPage.setBackgroundEntity(new PdfPageSerializable(newPage, document, pdfPage, document.tryGetPage(pdfPage.getNodeIndex() + 1)));
					pdfPage = pdfPage.getNextPage();
				}
			} else if (pdfMode == PDF_MODE_CLEAR) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Clear and create new
				clear();
				final DocumentPage firstPage = this.getPageByIndex(0);
				PDPage pdfPage = pageTree.getFirstPage();
				if (pdfPage != null) {
					firstPage.setBackgroundEntity(new PdfPageSerializable(firstPage, document, pdfPage, document.tryGetPage(pdfPage.getNodeIndex() + 1)));
					pdfPage = pdfPage.getNextPage();
				}
				while (pdfPage != null) {
					final DocumentPage newPage = this.addPage();
					newPage.setBackgroundEntity(new PdfPageSerializable(newPage, document, pdfPage, document.tryGetPage(pdfPage.getNodeIndex() + 1)));
					pdfPage = pdfPage.getNextPage();
				}
			}
		} else {
			if (pdfMode == PDF_MODE_CLEAR) {
				// Clear and reset whole document
				clear();
			} else if (pdfMode == PDF_MODE_REINDEX) {
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
