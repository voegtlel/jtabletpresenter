package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public interface IRenderable extends IEntity {
	/**
	 * Creates a clone of this renderable with a new id
	 * 
	 * @param id
	 *            id of clone
	 * 
	 * @return
	 */
	IRenderable cloneRenderable(long id);

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	void render(IPageBackRenderer renderer);

	/**
	 * Erase at a point and store history
	 * 
	 * @param eraseInfo
	 */
	void eraseAt(EraseInfo eraseInfo);

	/**
	 * Erases at the given info directly to this object.
	 * 
	 * @param collisionInfo
	 */
	void eraseAtDirect(CollisionInfo collisionInfo);

	/**
	 * Returns if the info is colliding with this object
	 * 
	 * @param collisionInfo
	 *            collision info
	 */
	boolean collides(CollisionInfo collisionInfo);
}