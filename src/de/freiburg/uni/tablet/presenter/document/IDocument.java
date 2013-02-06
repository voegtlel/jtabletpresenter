package de.freiburg.uni.tablet.presenter.document;

public interface IDocument extends IEntity {

	/**
	 * Returns the object at the given index or throws an exception.
	 * @param index
	 * @return
	 */
	DocumentPage getPageByIndex(int index);

	/**
	 * Counts the number of pages
	 * @return
	 */
	int getPageCount();
	
	/**
	 * Gets if the document has the given page
	 * 
	 * @param page
	 * @return true if page is contained
	 */
	boolean hasPage(DocumentPage page);
	
	/**
	 * Gets a page by id
	 * 
	 * @param id
	 * @return
	 */
	DocumentPage getPageById(long id);

	/**
	 * Gets a page index
	 * 
	 * @param page
	 * @return index or exception
	 */
	int getPageIndex(DocumentPage page);

	/**
	 * Generates a new id for objects
	 * @return
	 */
	long nextId();
	
	/**
	 * Verifies the given id
	 * @return
	 */
	void deserializeId(long id);

	/**
	 * Add a document listener
	 * @param listener
	 */
	void addListener(DocumentListener listener);

	/**
	 * Remove a document listener
	 * @param listener
	 */
	void removeListener(DocumentListener listener);

	/**
	 * Gets the next page or null
	 * @param page
	 * @return
	 */
	DocumentPage getNextPage(DocumentPage page);

	/**
	 * Gets the previous page or null
	 * @param page
	 * @return
	 */
	DocumentPage getPreviousPage(DocumentPage page);
}
