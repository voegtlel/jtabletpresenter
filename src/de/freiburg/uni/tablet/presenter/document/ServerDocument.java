package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;

public class ServerDocument extends Document {
	/**
	 * Reset indices and use first page as first pdf page
	 */
	public static final int PDF_MODE_REINDEX = 0;
	/**
	 * Keep current indices and append if needed
	 */
	public static final int PDF_MODE_KEEP_INDEX = 1;
	/**
	 * Append the loaded pdf to the end of the document
	 */
	public static final int PDF_MODE_APPEND = 2;
	/**
	 * Clear all pages and reindex
	 */
	public static final int PDF_MODE_CLEAR = 3;

	/**
	 * Create a new server document.
	 */
	public ServerDocument(final int docId) {
		super(docId);
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected ServerDocument(final int docId, final ServerDocument base) {
		super(docId, base);
	}
	
	/**
	 * Sets the background pdf
	 * @param document
	 */
	public void setPdfPages(final PdfSerializable document, int pdfMode) {
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
					docPage.getData().setPdfPage(new PdfPageSerializable(docPage.getData(), document, pdfPage));
					pdfPage = pdfPage.getNextPage();
					docPage = docPage.getNext();
				}
				while (docPage != null) {
					docPage.getData().setPdfPage(null);
					docPage = docPage.getNext();
				}
			} else if (pdfMode == PDF_MODE_KEEP_INDEX) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				int lastPdfIndex = 0;
				while (docPage.getNext() != null) {
					if (docPage.getData().getPdfPage().getPage().getNodeIndex() > lastPdfIndex) {
						lastPdfIndex = docPage.getData().getPdfPage().getPage().getNodeIndex();
					}
					docPage = docPage.getNext();
				}
				if (lastPdfIndex < pageTree.getCount() - 1) {
					PDPage pdfPage = pageTree.getPageAt(lastPdfIndex);
					while (pdfPage != null) {
						final DocumentPage newPage = this.addPage();
						newPage.setPdfPage(new PdfPageSerializable(newPage, document, pdfPage));
						pdfPage = pdfPage.getNextPage();
					}
				}
			} else if (pdfMode == PDF_MODE_APPEND) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Iterate through doc pages and find highest
				LinkedElement<DocumentPage> docPage = _pages.getFirst();
				while (docPage != null) {
					docPage.getData().setPdfPage(null);
					docPage = docPage.getNext();
				}
				PDPage pdfPage = pageTree.getFirstPage();
				while (pdfPage != null) {
					final DocumentPage newPage = this.addPage();
					newPage.setPdfPage(new PdfPageSerializable(newPage, document, pdfPage));
					pdfPage = pdfPage.getNextPage();
				}
			} else if (pdfMode == PDF_MODE_CLEAR) {
				final PDPageTree pageTree = document.getDocument().getPageTree();
				// Clear and create new
				clear();
				PDPage pdfPage = pageTree.getFirstPage();
				while (pdfPage != null) {
					final DocumentPage newPage = this.addPage();
					newPage.setPdfPage(new PdfPageSerializable(newPage, document, pdfPage));
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
					docPage.getData().setPdfPage(null);
					docPage = docPage.getNext();
				}
			} else {
				throw new IllegalStateException("Invalid pdfMode for null document");
			}
		}
	}

	/**
	 * Gets a page by its index
	 * 
	 * @param index
	 * @param createIfNotExisting
	 * @return
	 */
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
	
	/**
	 * Adds a document page
	 * 
	 * @return
	 * @throws IOException 
	 */
	public DocumentPage addPage(final BinaryDeserializer reader) throws IOException {
		final DocumentPage result = new DocumentPage(reader, this);
		final DocumentPage prevData = _pages.getLast() != null ? _pages
				.getLast().getData() : null;
		_pages.addLast(result);
		firePageInserted(prevData, result);
		return result;
	}
	
	/**
	 * Adds a document page
	 * 
	 * @return
	 */
	public DocumentPage addPage() {
		final DocumentPage result = new DocumentPage(this);
		final DocumentPage prevData = _pages.getLast() != null ? _pages
				.getLast().getData() : null;
		_pages.addLast(result);
		firePageInserted(prevData, result);
		return result;
	}

	/**
	 * Inserts a page after the given page.
	 * 
	 * @param afterPage
	 *            the previous page to the inserted page (null for first)
	 * @return
	 */
	public DocumentPage insertPage(final DocumentPage afterPage) {
		final DocumentPage result = new DocumentPage(this);
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, result);
			firePageInserted(element.getData(), result);
		} else {
			_pages.addFirst(result);
			firePageInserted(null, result);
		}
		return result;
	}

	/**
	 * Inserts a page after the given page.
	 * 
	 * @param afterPage
	 *            the previous page to the inserted page (null for first)
	 * @return
	 */
	public DocumentPage insertPage(final DocumentPage afterPage,
			final DocumentPage page) {
		if (page.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, page);
			firePageInserted(element.getData(), page);
		} else {
			_pages.addFirst(page);
			firePageInserted(null, page);
		}
		return page;
	}

	/**
	 * Removes a page.
	 * 
	 * @param page
	 *            the page to remove
	 */
	public void removePage(final DocumentPage page) {
		if (page.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		final LinkedElement<DocumentPage> element = _pages
				.getElementByInstance(page);
		if (element == null) {
			throw new IllegalArgumentException("afterPage not in document");
		}
		final LinkedElement<DocumentPage> prevPage = element.getPrevious();
		_pages.remove(element);
		firePageRemoved(prevPage.getData(), element.getData());
	}
	
	/**
	 * Removes all pages
	 */
	public void clear() {
		while (!_pages.isEmpty()) {
			final LinkedElement<DocumentPage> element = _pages.getFirst();
			_pages.removeFirst();
			firePageRemoved(null, element.getData());
		}
	}
	
	public ServerDocument(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	public ServerDocument clone(final int docId) {
		return new ServerDocument(docId, this);
	}
}
