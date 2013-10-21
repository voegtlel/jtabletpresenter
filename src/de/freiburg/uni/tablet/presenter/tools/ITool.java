package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;

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
	 */
	void end();

	/**
	 * Called, when the mouse is active for this tool
	 */
	void over();

	/**
	 * Called, when the mouse leaves this tool
	 */
	void out();

	/**
	 * Call to update cursor
	 */
	void invalidateCursor();
	
	/**
	 * Called after the document was modified and the tool may update its state
	 */
	void updateTool();
}
