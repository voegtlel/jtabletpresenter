package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

/**
 * Interface for a tool
 * 
 * @author lukas
 * 
 */
public interface ITool {
	/**
	 * Called, when drawing starts
	 */
	void begin();

	/**
	 * Called on every draw-update
	 * 
	 * @param data
	 */
	void draw(DataPoint data);

	/**
	 * Called, when drawing ends
	 * 
	 * @return
	 */
	IRenderable end();

	/**
	 * Called, when the mouse is active for this tool
	 */
	void over();

	/**
	 * Called, when the mouse leaves this tool
	 */
	void out();

	/**
	 * Returns, wheather the tool requires a redraw of the entire page.
	 * 
	 * @return
	 */
	boolean requiresRedraw();
}
