/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class Document {
	private final HashMap<Long, IEntity> _objects = new HashMap<Long, IEntity>();

	private final LinkedElementList<DocumentPage> _pages = new LinkedElementList<DocumentPage>();

	private final int _clientId;
	private int _uniqueId = 0;

	private DocumentPage _currentPage = null;

	/**
	 * 
	 */
	public Document(final int clientId) {
		_clientId = clientId;
	}

	/**
	 * Gets an object in the document
	 * 
	 * @param id
	 * @return
	 */
	public IEntity getObject(final long id) {
		return _objects.get(id);
	}

	/**
	 * Called, when an entity was added
	 * 
	 * @param entity
	 */
	void onObjectAdded(final IEntity entity) {
		_objects.put(entity.getId(), entity);
	}

	/**
	 * Called, when an entity was removed
	 * 
	 * @param entity
	 */
	void onObjectRemoved(final IEntity entity) {
		_objects.remove(entity.getId());
	}

	/**
	 * Adds a document page
	 * 
	 * @return
	 */
	public DocumentPage addPage() {
		final DocumentPage result = new DocumentPage(this);
		onObjectAdded(result);
		_pages.addLast(result);
		return result;
	}

	/**
	 * Inserts a page after the given page.
	 * 
	 * @param afterPage
	 *            the previous page to the inserted page (null for first)
	 * @return
	 */
	public DocumentPage insertPage(final DocumentPage afterPage) {
		final DocumentPage result = new DocumentPage(this);
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, result);
		} else {
			_pages.addFirst(result);
		}
		return result;
	}

	/**
	 * Returns the current page.
	 * 
	 * @return
	 */
	public DocumentPage getCurrentPage() {
		return _currentPage;
	}

	/**
	 * Set the current page
	 * 
	 * @param page
	 */
	public void setCurrentPage(final DocumentPage page) {
		final LinkedElement<DocumentPage> element = _pages
				.getElementByInstance(page);
		if (element == null) {
			throw new IllegalArgumentException("afterPage not in document");
		}
		_currentPage = element.getData();
	}

	/**
	 * Set the current page
	 * 
	 * @param page
	 */
	public void setCurrentPageByIndex(final int index) {
		final LinkedElement<DocumentPage> element = _pages
				.getElementByIndex(index);
		if (element == null) {
			throw new IllegalArgumentException("index out of range");
		}
		_currentPage = element.getData();
	}

	/**
	 * @return
	 */
	public long getNextId() {
		return (long) _clientId << 32 | _uniqueId++;
	}
}
