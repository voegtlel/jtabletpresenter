/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.tools.IToolContainer;

/**
 * @author lukas
 * 
 */
/**
 * @author luke
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
	int getPageIndex();

	/**
	 * Sets the current page by index
	 * 
	 * @param index
	 *            page index
	 */
	void setPageIndex(int index);

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

	/**
	 * Gets the renderer
	 * 
	 * @return renderer
	 */
	IPageRenderer getRenderer();

	/**
	 * Gets the tool container
	 * 
	 * @return tool container
	 */
	IToolContainer getToolContainer();

	/**
	 * Adds a listener
	 * 
	 * @param listener
	 */
	void addListener(IToolPageEditorListener listener);

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	void removeListener(IToolPageEditorListener listener);

	/**
	 * Gets the next if for objects and increases the object id counter.
	 * 
	 * @return next id for a new object
	 */
	int getNextObjectId();

	/**
	 * Gets, if the editor is in fullscreen mode
	 * @return is fullscreen
	 */
	boolean isFullscreen();

	/**
	 * Sets the fullscreen state
	 * @param fullscreen fullscreen state
	 */
	void setFullscreen(boolean fullscreen);
}
