package de.freiburg.uni.tablet.presenter.geometry;

public interface CollisionListener {

	/**
	 * Called when a collision was detected
	 * @param data
	 */
	void collides(IRenderable data);

}
