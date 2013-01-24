package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
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
	 * 
	 */
	void setParent(DocumentPage pageLayer);

	/**
	 * Erase at a point and store history
	 * 
	 * @param eraseInfo
	 */
	void eraseAt(EraseInfo eraseInfo);
	
	/**
	 * Ends the erase process
	 * @param eraseInfo 
	 * 
	 * @return if false, the object is now empty an thus can be removed
	 */
	boolean eraseEnd(EraseInfo eraseInfo);

	/**
	 * Returns if the info is colliding with this object
	 * 
	 * @param collisionInfo
	 *            collision info
	 */
	boolean collides(CollisionInfo collisionInfo);

	/**
	 * Gets the rectangle
	 * 
	 * @return
	 */
	float getMinX();

	/**
	 * Gets the rectangle
	 * 
	 * @return
	 */
	float getMinY();

	/**
	 * Gets the rectangle
	 * 
	 * @return
	 */
	float getMaxX();

	/**
	 * Gets the rectangle
	 * 
	 * @return
	 */
	float getMaxY();

	/**
	 * Gets the additional radius for redrawing in screen coordinates
	 * 
	 * @return
	 */
	float getRadius();
}