/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * 
 */
public class BinaryDeserializer {
	private final DataInputStream _dataInputStream;
	private final List<String> _stringTable = new ArrayList<String>();

	/**
	 * 
	 */
	public BinaryDeserializer(final InputStream stream) {
		_dataInputStream = new DataInputStream(stream);
	}

	public boolean readBoolean() throws IOException {
		return _dataInputStream.readBoolean();
	}

	public int readInt() throws IOException {
		return _dataInputStream.readInt();
	}

	public float readFloat() throws IOException {
		return _dataInputStream.readFloat();
	}

	public double readDouble() throws IOException {
		return _dataInputStream.readDouble();
	}

	public String readString() throws IOException {
		return _dataInputStream.readUTF();
	}

	public long readLong() throws IOException {
		return _dataInputStream.readLong();
	}

	public <T> T readSerializableClass() throws IOException {
		final String className = readStringTable();
		try {
			@SuppressWarnings("unchecked")
			final Class<T> classRef = (Class<T>) Class.forName(className);
			final Constructor<T> constructor = classRef
					.getConstructor(BinaryDeserializer.class);
			final T newInstance = constructor.newInstance(this);
			return newInstance;
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}

	public <T> T readSerializableClass(final Class<?>[] ctorArgTypes,
			final Object[] ctorArgs) throws IOException {
		final String className = readStringTable();
		try {
			@SuppressWarnings("unchecked")
			final Class<T> classRef = (Class<T>) Class.forName(className);
			final Constructor<T> constructor = classRef
					.getConstructor(ctorArgTypes);
			final T newInstance = constructor.newInstance(ctorArgs);
			return newInstance;
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}

	public String readStringTable() throws IOException {
		final int index = readInt();
		if ((index & 0x80000000) != 0) {
			final String newString = readString();
			if (_stringTable.size() != (index & 0x7fffffff)) {
				throw new IllegalStateException("String table out of synch");
			}
			_stringTable.add(newString);
			return newString;
		} else {
			return _stringTable.get(index & 0x7fffffff);
		}
	}

	public <T> T readObjectTable(final List<T> table) throws IOException {
		final boolean isNew = readBoolean();
		if (isNew) {
			final T objectRef = this.<T> readSerializableClass();
			table.add(objectRef);
			return objectRef;
		} else {
			final int index = readInt();
			return table.get(index);
		}
	}
}
