package de.freiburg.uni.tablet.presenter.document.document;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;

public interface IBackDocumentPage {
	/**
	 * Creates an entity from the page
	 * @param parent
	 * @return
	 */
	IBackDocumentPageEntity createEntity(DocumentPage parent);
	/**
	 * Gets the page index
	 * @return
	 */
	int getPageIndex();
}
