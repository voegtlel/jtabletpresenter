package de.freiburg.uni.tablet.presenter.document;


public interface IEditableDocument extends IClientDocument {
	/**
	 * Reset indices and use first page as first pdf page
	 */
	int PDF_MODE_REINDEX = 0;
	/**
	 * Keep current indices and append if needed
	 */
	int PDF_MODE_KEEP_INDEX = 1;
	/**
	 * Append the loaded pdf to the end of the document
	 */
	int PDF_MODE_APPEND = 2;
	/**
	 * Clear all pages and reindex
	 */
	int PDF_MODE_CLEAR = 3;

	/**
	 * Sets the background pdf
	 * @param document
	 */
	void setPdfPages(PdfSerializable document, int pdfMode);

	/**
	 * Gets a page by its index
	 * 
	 * @param index
	 * @param createIfNotExisting
	 * @return
	 */
	DocumentPage getPageByIndex(int index,
			boolean createIfNotExisting);

	/**
	 * Adds a document page
	 * 
	 * @return
	 */
	DocumentPage addPage();

	/**
	 * Inserts a page after the given page.
	 * 
	 * @param afterPage
	 *            the previous page to the inserted page (null for first)
	 * @return
	 */
	DocumentPage insertPage(DocumentPage afterPage);

	/**
	 * Removes all pages
	 */
	void clear();

	/**
	 * Clones the document
	 * @param docId
	 * @return
	 */
	IEditableDocument clone(int docId);

	/**
	 * Clones a page
	 * @param page
	 * @return
	 */
	DocumentPage clonePage(DocumentPage page);

	/**
	 * Gets the next page or throws exception
	 * @param page
	 * @param createIfNotExists
	 * @return
	 */
	DocumentPage getNextPage(DocumentPage page, boolean createIfNotExists);

}