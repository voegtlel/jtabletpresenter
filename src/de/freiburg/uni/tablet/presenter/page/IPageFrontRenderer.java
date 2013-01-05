package de.freiburg.uni.tablet.presenter.page;

import java.awt.image.BufferedImage;

public interface IPageFrontRenderer {
	/**
	 * Draws a line to the front surface. This is cleared on refresh.
	 * 
	 * @param pen
	 *            pen
	 */
	void draw(IPen pen, float x1, float y1, float x2, float y2);

	/**
	 * Draws a line to the front surface. This is cleared on refresh.
	 * 
	 * @param pen
	 *            pen
	 * @param p
	 *            location
	 */
	void draw(IPen pen, float x, float y);
	
	/**
	 * Draws an image to the front surface. This is cleared on refresh.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void draw(BufferedImage image, float x, float y, float width, float height);

	/**
	 * Clears the front buffer
	 */
	void clear();
}
