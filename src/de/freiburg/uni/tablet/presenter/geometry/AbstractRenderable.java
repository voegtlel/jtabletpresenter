package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

public abstract class AbstractRenderable implements IBinarySerializable,
		IRenderable {
	private final long _id;

	/**
	 * Creates the renderable
	 */
	protected AbstractRenderable(final long id) {
		_id = id;
	}

	/**
	 * Creates the renderable from an input stream
	 * 
	 * @throws IOException
	 */
	protected AbstractRenderable(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
	}

	/**
	 * Returns the id of the renderable object.
	 * 
	 * @return id
	 */
	@Override
	public long getId() {
		return _id;
	}

	@Override
	public void serialize(final BinarySerializer stream) throws IOException {
		stream.writeLong(_id);
	}
}