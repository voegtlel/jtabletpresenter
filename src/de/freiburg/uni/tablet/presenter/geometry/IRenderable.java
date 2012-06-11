package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;

public abstract class IRenderable implements IBinarySerializable {
	private static int __nextId = 0;
	private final int _id;

	/**
	 * Creates the renderable
	 */
	protected IRenderable() {
		_id = (__nextId++);
	}

	/**
	 * Creates the renderable from an input stream
	 * 
	 * @throws IOException
	 */
	protected IRenderable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readInt();
	}

	/**
	 * Renders the object
	 * 
	 * @param renderer
	 *            renderer
	 */
	public abstract void render(IPageRenderer renderer);

	/**
	 * Erase at a point
	 * 
	 * @param eraseInfo
	 */
	public abstract void eraseAt(EraseInfo eraseInfo);

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

	public static void serializeStatic(final BinarySerializer stream)
			throws IOException {
		stream.writeInt(__nextId);
	}

	public static void deserializeStatic(final BinaryDeserializer stream)
			throws IOException {
		__nextId = stream.readInt();
	}
}