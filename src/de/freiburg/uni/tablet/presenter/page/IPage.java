package de.freiburg.uni.tablet.presenter.page;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IPage {
	/**
	 * Add a renderable object to the page.
	 * 
	 * @param renderable
	 *            object to add
	 */
	void addRenderable(IRenderable renderable);

	/**
	 * Remove a renderable object from the page.
	 * 
	 * @param renderable
	 *            object to remove
	 */
	void removeRenderable(IRenderable renderable);

}
