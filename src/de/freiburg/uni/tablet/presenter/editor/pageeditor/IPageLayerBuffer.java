package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.graphics.Canvas;

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
	void drawBuffer(final Canvas g);
}