package de.freiburg.uni.tablet.presenter.page;

public interface IPageFrontRenderer {
	/**
	 * Draws a line to the front surface. This is cleared on refresh.
	 * 
	 * @param pen
	 *            pen
	 */
	void drawFront(IPen pen, float x1, float y1, float x2, float y2);

	/**
	 * Draws a line to the front surface. This is cleared on refresh.
	 * 
	 * @param pen
	 *            pen
	 * @param p
	 *            location
	 */
	void drawFront(IPen pen, float x, float y);

	/**
	 * Clears the front buffer
	 */
	void clearFront();
}
