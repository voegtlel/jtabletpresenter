package de.freiburg.uni.tablet.presenter.page;

import java.awt.Paint;
import java.awt.geom.Path2D;

public interface IPageBackRenderer {
	/**
	 * Draws a line for the background grid.
	 * 
	 * @param pen
	 *            pen
	 */
	void drawGridLine(IPen pen, float x1, float y1, float x2, float y2);

	/**
	 * Renders a path.
	 * 
	 * @param pen
	 *            pen
	 * @param path
	 *            path
	 */
	void draw(IPen pen, Path2D path);

	/**
	 * Renders a dot using the pen.
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	void draw(IPen pen, float x, float y);

	/**
	 * Fills everything
	 * 
	 * @param paint
	 */
	void fill(Paint paint);

	/**
	 * Redraws the entire back buffer
	 */
	void redrawBack();
}
