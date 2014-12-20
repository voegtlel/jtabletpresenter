package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IToolListener {
	/**
	 * Called when the tool has finished an editing step and created a renderable.
	 */
	void onFinish(IRenderable createdRenderable);
	
	/**
	 * Called when the tool is starting an editing step (may consist of multiple clicks).
	 */
	void onStart();
}
