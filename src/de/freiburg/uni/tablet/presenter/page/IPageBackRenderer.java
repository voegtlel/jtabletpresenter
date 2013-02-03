package de.freiburg.uni.tablet.presenter.page;

import android.graphics.Bitmap;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

public interface IPageBackRenderer {
	/**
	 * Renders a path.
	 * 
	 * @param pen
	 *            pen
	 * @param path
	 *            path
	 */
	//void draw(IPen pen, Path path);

	/**
	 * Renders a dot using the pen.
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	void draw(IPen pen, float x, float y);
	
	/**
	 * 
	 * @param pen
	 * @param path
	 */
	void draw(IPen pen, LinkedElementList<DataPoint> path);
	
	/**
	 * Renders an image.
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void draw(Bitmap image, float x, float y, float width, float height);
	
	/**
	 * Renders the eraser
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	void drawEraser(IPen pen, float x, float y);

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
