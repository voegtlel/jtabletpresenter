package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface DocumentListener {
	/**
	 * Called, when a page was inserted
	 * 
	 * @param prevPage
	 * @param page
	 */
	void pageInserted(DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a page was removed
	 * 
	 * @param index
	 * @param page
	 */
	void pageRemoved(DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a renderable was added
	 * 
	 * @param renderable
	 * @param layer
	 */
	void renderableAdded(IRenderable renderable, DocumentPageLayer layer);

	/**
	 * Called, when a renderable was removed
	 * 
	 * @param renderable
	 * @param layer
	 */
	void renderableRemoved(IRenderable renderable, DocumentPageLayer layer);
}
