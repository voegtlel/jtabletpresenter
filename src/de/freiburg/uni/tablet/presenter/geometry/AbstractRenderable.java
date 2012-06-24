package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public abstract class AbstractRenderable implements IBinarySerializable {
	private final int _id;

	/**
	 * Creates the renderable
	 */
	protected AbstractRenderable(final int id) {
		_id = id;
	}

	/**
	 * Creates the renderable from an input stream
	 * 
	 * @throws IOException
	 */
	protected AbstractRenderable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readInt();
	}

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	public abstract void render(IPageBackRenderer renderer);

	/**
	 * Erase at a point
	 * 
	 * @param eraseInfo
	 */
	public abstract void eraseAt(EraseInfo eraseInfo);

	/**
	 * Returns if the info is colliding with this object
	 * 
	 * @param collisionInfo
	 *            collision info
	 */
	public abstract boolean isInRange(CollisionInfo collisionInfo);

	/**
	 * Returns the id of the renderable object.
	 * 
	 * @return id
	 */
	public int getId() {
		return _id;
	}

	@Override
	public void serialize(final BinarySerializer stream) throws IOException {
		stream.writeInt(_id);
	}
}