package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public abstract class AbstractPageEntity implements IEntity {
	private final long _id;
	protected final DocumentPage _parent;

	/**
	 * Creates the renderable
	 */
	protected AbstractPageEntity(final DocumentPage parent) {
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
	protected AbstractPageEntity(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.getParent().deserializeId(_id);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
	}
	
	@Override
	public String toString() {
		return String.format("Renderable %s (%X)", this.getClass().getSimpleName(), System.identityHashCode(this));
	}
}