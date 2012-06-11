/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.page.IPen;

/**
 * @author lukas
 * 
 */
public interface IToolPageEditor extends IPageEditor {
	/**
	 * Gets an iterable for all pages
	 * 
	 * @return iterable for all pages
	 */
	Iterable<IPage> getPages();

	/**
	 * Add a page at the specified index
	 * 
	 * @return
	 */
	IPage addPage();

	/**
	 * Add a page at the specified index
	 * 
	 * @param index
	 *            insertion index
	 * @return
	 */
	IPage addPage(int index);

	/**
	 * Gets the number of (nonempty) pages
	 * 
	 * @return number of (nonempty) pages
	 */
	int getPageCount();

	/**
	 * Gets the index of the current page
	 * 
	 * @return index of current page
	 */
	int getCurrentPage();

	/**
	 * Sets the current page by index
	 * 
	 * @param index
	 *            page index
	 */
	void setCurrentPage(int index);

	/**
	 * Sets the current pen
	 * 
	 * @param pen
	 *            new pen
	 */
	void setCurrentPen(IPen pen);

	/**
	 * Gets the current pen
	 * 
	 * @return current pen
	 */
	IPen getCurrentPen();
}
