package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.graphics.Bitmap;

public interface IDisplayRenderer {
	/**
	 * Creates an image for buffering
	 * 
	 * @return new image
	 */
	public Bitmap createImageBuffer(int width, int height);

	/**
	 * Returns, if we can work with this
	 * 
	 * @return
	 */
	public boolean isWorking();
	
	/**
	 * Repaint is required (only draws the buffers, buffer repainting is managed in buffers)
	 */
	public void requireRepaint();

	/**
	 * Stops further repainting
	 */
	void suspendRepaint();

	/**
	 * Resume repainting
	 */
	void resumeRepaint();
}
