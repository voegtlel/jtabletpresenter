package de.freiburg.uni.tablet.presenter.document.document;

import de.freiburg.uni.tablet.presenter.document.IEntity;

public interface IBackDocument extends IEntity {
	/**
	 * Gets all pages
	 * @return
	 */
	Iterable<IBackDocumentPage> getPages();
	/**
	 * Gets the number of pages
	 * @return
	 */
	int getPageCount();
	/**
	 * Returns an iterator starting at the given index
	 * @param pageIndex start index
	 * @return iterator
	 */
	Iterable<IBackDocumentPage> getPagesAt(int pageIndex);
}
