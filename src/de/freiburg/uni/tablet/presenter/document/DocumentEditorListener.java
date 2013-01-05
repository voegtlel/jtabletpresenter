package de.freiburg.uni.tablet.presenter.document;

public interface DocumentEditorListener {

	/**
	 * Fired, when the current page was changed
	 * 
	 * @param lastCurrentPage
	 */
	void currentPageChanged(DocumentPage lastCurrentPage);

	/**
	 * Fired when the active layer was changed
	 * 
	 * @param lastActiveLayerClientOnly
	 */
	void activeLayerChanged(boolean lastActiveLayerClientOnly);

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
