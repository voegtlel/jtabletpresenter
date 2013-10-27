package de.freiburg.uni.tablet.presenter.page;

import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IPageBackRenderer {
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
	 * Renders an image.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void draw(BufferedImage image, float x, float y, float width, float height);
	
	/**
	 * Draws a line
	 * 
	 * @param pen
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	void draw(IPen pen, float x1, float y1, float x2, float y2);
	
	/**
	 * Sets the rendering offset
	 * @param x
	 * @param y
	 */
	void setOffset(float x, float y);
	
	/**
	 * Draws a string using the given font. Attention: The text is rendered in -y direction. x,y specify the baseline.
	 * @param x
	 * @param y
	 * @param text
	 * @param font
	 */
	void draw(float x, float y, String text, TextFont font);

	/**
	 * Sets the repaint listener
	 * 
	 * @param repaintListener
	 */
	void setRepaintListener(IPageRepaintListener repaintListener);
	
	/**
	 * Requires full repainting on next render. Notifies display.
	 */
	void requireRepaint();
	
	/**
	 * Requires repainting the given renderable.
	 * 
	 * @param renderable the renderable to (re)draw
	 * @param clear if true, the rectangle below the renderable is cleared and also redrawn,
	 * otherwise only the renderable is added
	 */
	void requireRepaint(IRenderable renderable, boolean clear);
}
