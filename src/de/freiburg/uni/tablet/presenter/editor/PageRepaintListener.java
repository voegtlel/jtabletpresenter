package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public interface PageRepaintListener {
	/**
	 * Fired, when the page requires to repaint.
	 * 
	 * @param renderer
	 *            target renderer
	 */
	void render(IPageBackRenderer renderer);
}
