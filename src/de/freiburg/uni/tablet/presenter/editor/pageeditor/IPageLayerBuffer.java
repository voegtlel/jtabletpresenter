package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;

public interface IPageLayerBuffer {

	/**
	 * Called when the surface is resized
	 * 
	 * @param width
	 * @param height
	 */
	void resize(final int width, final int height);

	/**
	 * Renders the buffer to the given graphics
	 * 
	 * @param g
	 */
	void drawBuffer(final Graphics2D g);

	/**
	 * Clears the buffer
	 */
	void clear();

}