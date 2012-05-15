package de.freiburg.uni.tablet.presenter.page;

import java.awt.geom.Path2D;

public interface IPageRenderer {
	/**
	 * Draws a line for the background grid.
	 * 
	 * @param pen
	 *            pen
	 */
	void drawGridLine(IPen pen, float x1, float y1, float x2, float y2);

	/**
	 * Renders a path. Must check, if there is only one element in the path.
	 * 
	 * @param pen
	 *            pen
	 * @param path
	 *            path
	 */
	void draw(IPen pen, Path2D path);

	/**
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	void draw(IPen pen, float x, float y);

}
