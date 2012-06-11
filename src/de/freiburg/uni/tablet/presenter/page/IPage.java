package de.freiburg.uni.tablet.presenter.page;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public abstract class IPage extends IRenderable {
	/**
	 * Add a renderable object to the page.
	 * 
	 * @param renderable
	 *            object to add
	 */
	public abstract void addRenderable(IRenderable renderable);

	/**
	 * Remove a renderable object from the page.
	 * 
	 * @param renderable
	 *            object to remove
	 */
	public abstract void removeRenderable(IRenderable renderable);
}
