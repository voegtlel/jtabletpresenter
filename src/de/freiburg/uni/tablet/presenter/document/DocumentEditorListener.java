package de.freiburg.uni.tablet.presenter.document;

public interface DocumentEditorListener {

	/**
	 * Fired, when the current page was changed
	 * 
	 * @param lastCurrentPage
	 * @param lastCurrentBackPage
	 */
	void currentPageChanged(DocumentPage lastCurrentPage, DocumentPage lastCurrentBackPage);

	/**
	 * Fired when the document was changed
	 * 
	 * @param lastDocument
	 */
	void documentChanged(Document lastDocument);

	/**
	 * Fired when something is about to change
	 */
	void changing();

}
