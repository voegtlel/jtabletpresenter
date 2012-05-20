package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.page.IPageRenderer;

public interface IRenderable {

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	void render(final IPageRenderer renderer);

}