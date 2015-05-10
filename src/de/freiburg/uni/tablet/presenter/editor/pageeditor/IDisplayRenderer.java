package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.image.BufferedImage;

public interface IDisplayRenderer {
	/**
	 * Creates an image for buffering
	 * 
	 * @return new buffered image
	 */
	public BufferedImage createImageBuffer(int width, int height, int transparency);

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
