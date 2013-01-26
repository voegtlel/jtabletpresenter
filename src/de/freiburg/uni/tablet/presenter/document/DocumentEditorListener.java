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
	void documentChanged(IEditableDocument lastDocument);
	
	/**
	 * Fired when the baseDocument was changed
	 * 
	 * @param lastDocument
	 */
	void baseDocumentChanged(IDocument lastDocument);

	/**
	 * Fired when something is about to change
	 */
	void changing();

}
