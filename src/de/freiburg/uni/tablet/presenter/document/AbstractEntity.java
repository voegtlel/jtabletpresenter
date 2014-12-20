package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;

/**
 * Abstract class for an entity.
 * 
 * @author Lukas
 *
 */
public abstract class AbstractEntity implements IEntity {
	private final long _id;
	private final IDocument _parent;

	public AbstractEntity(final IDocument parent) {
		_parent = parent;
		_id = parent.nextId();
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public IDocument getParent() {
		return _parent;
	}

	protected AbstractEntity(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.deserializeId(_id);
	}

	@Override
	synchronized public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
	}

	@Override
	public String toString() {
		return String.format("Entity %s (%X)", this.getClass().getSimpleName(), System.identityHashCode(this));
	}
}