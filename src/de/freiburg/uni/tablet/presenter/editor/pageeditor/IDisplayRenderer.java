package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.Image;

public interface IDisplayRenderer {
	/**
	 * Gets the graphics for rendering
	 * 
	 * @return graphics object for rendering
	 */
	public Graphics2D createRenderer();

	/**
	 * Updates the renderer
	 * 
	 * @param g
	 *            graphics, previously created with createRenderer
	 */
	public void updateRenderer(Graphics2D g);

	/**
	 * Gets the width
	 * 
	 * @return
	 */
	public int getWidth();

	/**
	 * Gets the height
	 * 
	 * @return
	 */
	public int getHeight();

	/**
	 * Creates an image for buffering
	 * 
	 * @return new image
	 */
	public Image createImageBuffer(int width, int height, int transparency);

	/**
	 * Returns, if we can work with this
	 * 
	 * @return
	 */
	public boolean isWorking();
}
