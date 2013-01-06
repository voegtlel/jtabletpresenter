/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;

/**
 * @author lukas
 * 
 */
public class Document implements IEntity {
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
	
	private final LinkedElementList<DocumentPage> _pages = new LinkedElementList<DocumentPage>();

	private final int _clientId;
	private int _uniqueId = 1;

	private final List<DocumentListener> _listeners = new LinkedList<DocumentListener>();
	
	private PdfSerializable _pdfDocument;

	/**
	 * 
	 */
	public Document(final int clientId) {
		_clientId = clientId;
		_pdfDocument = new PdfSerializable(getNextId(), this);
		addPage();
	}
	
	/**
	 * Sets the background pdf
	 * @param document
	 */
	public void setPdf(final File file, int pdfMode) throws IOException {
		setPdf(new PdfSerializable(getNextId(), this, file), pdfMode);
	}
	
	/**
	 * Sets the background pdf
	 * @param document
	 */
	public void setPdf(final PDDocument document, int pdfMode) {
		setPdf(new PdfSerializable(getNextId(), this, document), pdfMode);
	}
	
	/**
	 * Sets the background pdf
	 * @param document
	 */
	public void setPdf(final PdfSerializable document, int pdfMode) {
		PdfSerializable lastPdf = _pdfDocument;
		_pdfDocument = document;
		if (pdfMode == PDF_MODE_REINDEX) {
			PDPageTree pageTree = document.getDocument().getPageTree();
			// Ensure the pages exist (enough space)
			getPageByIndex(pageTree.getCount() - 1, true);
			// Iterate through pdf pages
			PDPage pdfPage = pageTree.getFirstPage();
			LinkedElement<DocumentPage> docPage = _pages.getFirst();
			while (pdfPage != null) {
				// Set indices
				docPage.getData().setPdfPageIndex(pdfPage.getNodeIndex());
				pdfPage = pdfPage.getNextPage();
				docPage = docPage.getNext();
			}
			while (docPage != null) {
				docPage.getData().setPdfPageIndex(-1);
				docPage = docPage.getNext();
			}
		} else if (pdfMode == PDF_MODE_KEEP_INDEX) {
			PDPageTree pageTree = document.getDocument().getPageTree();
			// Iterate through doc pages and find highest
			LinkedElement<DocumentPage> docPage = _pages.getFirst();
			int lastPdfIndex = 0;
			while (docPage.getNext() != null) {
				if (docPage.getData().getPdfPageIndex() > lastPdfIndex) {
					lastPdfIndex = docPage.getData().getPdfPageIndex();
				}
				docPage = docPage.getNext();
			}
			if (lastPdfIndex < pageTree.getCount() - 1) {
				PDPage pdfPage = pageTree.getPageAt(lastPdfIndex);
				while (pdfPage != null) {
					DocumentPage newPage = this.addPage();
					newPage.setPdfPageIndex(pdfPage.getNodeIndex());
					pdfPage = pdfPage.getNextPage();
				}
			}
		} else if (pdfMode == PDF_MODE_APPEND) {
			PDPageTree pageTree = document.getDocument().getPageTree();
			// Iterate through doc pages and find highest
			LinkedElement<DocumentPage> docPage = _pages.getFirst();
			while (docPage != null) {
				docPage.getData().setPdfPageIndex(-1);
				docPage = docPage.getNext();
			}
			PDPage pdfPage = pageTree.getFirstPage();
			while (pdfPage != null) {
				DocumentPage newPage = this.addPage();
				newPage.setPdfPageIndex(pdfPage.getNodeIndex());
				pdfPage = pdfPage.getNextPage();
			}
		}
		firePdfChanged(lastPdf);
	}
	
	/**
	 * Gets the background pdf
	 * @return
	 */
	public PdfSerializable getPdf() {
		return _pdfDocument;
	}

	/**
	 * Gets a page by its index
	 * 
	 * @param index
	 * @return
	 */
	public DocumentPage getPageByIndex(final int index,
			final boolean createIfNotExisting) {
		int i = index;
		for (LinkedElement<DocumentPage> j = _pages.getFirst(); j != null; j = j
				.getNext()) {
			if (i == 0) {
				return j.getData();
			}
			i--;
		}
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
	 * Gets a pages index
	 * 
	 * @param page
	 * @return
	 */
	public int getPageIndex(final DocumentPage page) {
		return _pages.getElementIndex(page);
	}
	
	/**
	 * Adds a document page
	 * 
	 * @return
	 * @throws IOException 
	 */
	public DocumentPage addPage(BinaryDeserializer reader) throws IOException {
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
	
	public LinkedElement<DocumentPage> getPages() {
		return _pages.getFirst();
	}

	/**
	 * @return
	 */
	public long getNextId() {
		return (long) _clientId << 32 | _uniqueId++;
	}

	@Override
	public long getId() {
		return _clientId << 32;
	}
	
	public int getClientId() {
		return _clientId;
	}

	@Override
	public IEntity getParent() {
		return null;
	}

	void firePageInserted(final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageInserted(prevPage, page);
		}
	}

	void firePageRemoved(final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageRemoved(prevPage, page);
		}
	}

	void fireRenderableAdded(final IRenderable renderable,
			final DocumentPageLayer layer) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdded(renderable, layer);
		}
	}

	void fireRenderableRemoved(final IRenderable renderable,
			final DocumentPageLayer layer) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoved(renderable, layer);
		}
	}
	
	void firePdfChanged(final PdfSerializable lastPdf) {
		for (final DocumentListener listener : _listeners) {
			listener.pdfChanged(lastPdf);
		}
	}

	public void addListener(final DocumentListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(final DocumentListener listener) {
		_listeners.remove(listener);
	}

	public Document(final BinaryDeserializer reader) throws IOException {
		_uniqueId = reader.readInt();
		_clientId = reader.readInt();
		reader.putObjectTable(this.getId(), this);
		_pdfDocument = reader.readObjectTable();
		final int pageCount = reader.readInt();
		for (int i = 0; i < pageCount; i++) {
			final DocumentPage page = reader.readObjectTable();
			_pages.addLast(page);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_uniqueId);
		writer.writeInt(_clientId);
		writer.writeObjectTable(_pdfDocument.getId(), _pdfDocument);
		// Always has first
		writer.writeInt(_pages.getFirst().getNextCount());
		for (LinkedElement<DocumentPage> dp = _pages.getFirst(); dp != null; dp = dp
				.getNext()) {
			writer.writeObjectTable(dp.getData().getId(), dp.getData());
		}
	}
}
