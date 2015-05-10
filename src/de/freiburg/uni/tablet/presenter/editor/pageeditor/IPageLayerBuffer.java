package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;

public interface IPageLayerBuffer {

	/**
	 * Called when the surface is resized
	 * 
	 * @param renderMetric
	 */
	void resize(final RenderMetric renderMetric);

	/**
	 * Renders the buffer to the given graphics
	 * 
	 * @param g
	 */
	void drawBuffer(final Graphics2D g, RenderMetric renderMetric);

	/**
	 * Gets the desired ratio of this layer buffer or null if there is no preference.
	 * @return null or desired ratio
	 */
	Float getDesiredRatio();
}