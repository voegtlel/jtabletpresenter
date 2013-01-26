package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

public abstract class AbstractRenderable implements IBinarySerializableId,
		IRenderable {
	private final long _id;
	protected final DocumentPage _parent;

	/**
	 * Creates the renderable
	 */
	protected AbstractRenderable(final DocumentPage parent) {
		_parent = parent;
		_id = parent.getParent().nextId();
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
	public DocumentPage getParent() {
		return _parent;
	}

	/**
	 * Creates the renderable from an input stream
	 * 
	 * @throws IOException
	 */
	protected AbstractRenderable(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
	}
}