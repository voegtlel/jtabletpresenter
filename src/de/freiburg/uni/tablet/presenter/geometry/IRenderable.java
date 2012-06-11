package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;

public interface IRenderable extends IBinarySerializable {

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	void render(IPageRenderer renderer);

	/**
	 * Erase at a point
	 * 
	 * @param eraseInfo
	 */
	void eraseAt(EraseInfo eraseInfo);

}