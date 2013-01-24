package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;


public interface IRepaintListener {
	/**
	 * Fired, when the page requires to repaint.
	 */
	void render(final IPageBackRenderer renderer);
}
