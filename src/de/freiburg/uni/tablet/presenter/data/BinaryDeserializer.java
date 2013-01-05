/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lukas
 * 
 */
public class BinaryDeserializer {
	private final DataInputStream _dataInputStream;
	private final List<String> _stringTable = new ArrayList<String>();
	private final HashMap<Long, IBinarySerializable> _objectTable = new HashMap<Long, IBinarySerializable>();

	private static final Class<?>[] _defaultCtorArgTypes = new Class<?>[] { BinaryDeserializer.class };
	private final Object[] _defaultCtorArgs = new Object[] { this };

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
	
	public byte[] readByteArray() throws IOException {
		int size = _dataInputStream.readInt();
		byte[] result = new byte[size];
		while (size > 0) {
			int read = _dataInputStream.read(result, result.length - size, size);
			if (read < 0) {
				throw new IOException("Unexpected end of stream");
			}
			size -= read;
		}
		return result;
	}

	public <T> T readSerializableClass() throws IOException {
		return readSerializableClass(_defaultCtorArgTypes, _defaultCtorArgs);
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
		} catch (final InvocationTargetException e) {
			throw new IOException(e.getTargetException());
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

	/**
	 * Used to ensure an object is in the object table
	 * 
	 * @param identifier
	 * @param obj
	 */
	public void putObjectTable(final long identifier,
			final IBinarySerializable obj) {
		_objectTable.put(identifier, obj);
	}

	public <T extends IBinarySerializable> T readObjectTable(
			final Class<?>[] ctorArgTypes, final Object[] ctorArgs)
			throws IOException {
		final long identifier = readLong();
		if (identifier == 0) {
			return null;
		} else if ((identifier & 0x8000000000000000l) != 0) {
			final T newObj = readSerializableClass(ctorArgTypes, ctorArgs);
			_objectTable.put(identifier & 0x7fffffffffffffffl, newObj);
			return newObj;
		} else {
			@SuppressWarnings("unchecked")
			final T res = (T) _objectTable.get(identifier);
			if (res == null) {
				throw new IOException(
						"Can't read object table: Object not existing");
			}
			return res;
		}
	}

	public <T extends IBinarySerializable> T readObjectTable()
			throws IOException {
		return readObjectTable(_defaultCtorArgTypes, _defaultCtorArgs);
	}
}
