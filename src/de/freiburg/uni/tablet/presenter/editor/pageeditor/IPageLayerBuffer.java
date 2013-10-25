package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public interface IPageLayerBuffer {

	/**
	 * Called when the surface is resized
	 * 
	 * @param width
	 * @param height
	 * @param offsetX
	 * @param offsetY
	 */
	void resize(final int width, final int height, int offsetX, int offsetY);

	/**
	 * Renders the buffer to the given graphics
	 * 
	 * @param g
	 */
	void drawBuffer(final Graphics2D g, ImageObserver obs);

	/**
	 * Gets the desired ratio of this layer buffer or null if there is no preference.
	 * @return null or desired ratio
	 */
	Float getDesiredRatio();
}