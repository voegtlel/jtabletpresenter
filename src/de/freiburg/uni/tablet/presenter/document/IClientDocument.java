package de.freiburg.uni.tablet.presenter.document;

public interface IClientDocument extends IDocument {

	/**
	 * 
	 * @param afterPage
	 * @param page
	 * @return
	 */
	DocumentPage insertPage(DocumentPage afterPage, DocumentPage page);

	/**
	 * Removes a page.
	 * 
	 * @param page
	 *            the page to remove
	 */
	void removePage(DocumentPage page);

	/**
	 * Clones the document
	 * @param docId
	 * @return
	 */
	IClientDocument clone(int docId);
}
