package de.freiburg.uni.tablet.presenter.page;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.geometry.CollisionInfo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public abstract class IPage extends IRenderable {
	/**
	 * @param id
	 */
	protected IPage() {
		super(0);
	}

	/**
	 * @param reader
	 * @throws IOException
	 */
	protected IPage(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}

	/**
	 * Add a renderable object to the page.
	 * 
	 * @param renderable
	 *            object to add
	 */
	public abstract void addRenderable(IRenderable renderable);

	/**
	 * Remove a renderable object from the page.
	 * 
	 * @param renderable
	 *            object to remove
	 */
	public abstract void removeRenderable(IRenderable renderable);

	/**
	 * Gets the next if for objects and increases the object id counter.
	 * 
	 * @return next id for a new object
	 */
	public abstract int getNextObjectId();

	@Override
	public boolean isInRange(final CollisionInfo collisionInfo) {
		return true;
	}
}
