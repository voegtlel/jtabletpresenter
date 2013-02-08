package de.freiburg.uni.tablet.presenter.document.editor;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.page.IPen;

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
	void documentChanged(IClientDocument lastDocument);
	
	/**
	 * Fired when the baseDocument was changed
	 * 
	 * @param lastDocument
	 */
	void backDocumentChanged(IDocument lastDocument);

	/**
	 * Fired when something is about to change
	 */
	void changing();
	
	/**
	 * Fired when the current pen was changed
	 * @param lastPen
	 */
	void currentPenChanged(IPen lastPen);

}
