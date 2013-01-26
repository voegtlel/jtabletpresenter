package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public interface IRenderable extends IEntity {
	/**
	 * Creates a clone of this renderable with a new id
	 * 
	 * @param parent
	 * 
	 * @return
	 */
	IRenderable cloneRenderable(DocumentPage parent);

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	void render(IPageBackRenderer renderer);

	/**
	 * Begin erasing this instance.
	 * @param eraseInfo
	 * @param replacingInstance the instance replacing this renderable after the erase
	 * @return false if the object is empty and should be removed
	 */
	boolean eraseStart(EraseInfo eraseInfo);
	
	/**
	 * Erase at a point.
	 * 
	 * @param eraseInfo
	 * @return false if the object is empty and should be removed
	 */
	boolean eraseAt(EraseInfo eraseInfo);
	
	/**
	 * Ends the erase process.
	 * @param eraseInfo 
	 * 
	 * @return false if the object is empty and should be removed
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