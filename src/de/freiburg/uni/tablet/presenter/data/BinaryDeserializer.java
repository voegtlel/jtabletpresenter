/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lukas
 * 
 */
public class BinaryDeserializer {
	private final ByteBuffer _buffer = ByteBuffer.allocateDirect(128);
	private final ReadableByteChannel _channel;
	private final List<String> _stringTable = new ArrayList<String>();
	private final HashMap<Long, IBinarySerializableId> _objectTable = new HashMap<Long, IBinarySerializableId>();

	private static final Class<?>[] _defaultCtorArgTypes = new Class<?>[] { BinaryDeserializer.class };
	private final Object[] _defaultCtorArgs = new Object[] { this };

	/**
	 * 
	 */
	public BinaryDeserializer(final ReadableByteChannel channel) {
		_channel = channel;
	}
	
	public void read(final ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			final int read = _channel.read(buffer);
			if (read < 0) {
				throw new EOFException("result: " + read);
			}
		}
	}

	public boolean readBoolean() throws IOException {
		_buffer.clear();
		_buffer.limit(1);
		read(_buffer);
		_buffer.flip();
		return _buffer.get() != 0;
	}

	public int readInt() throws IOException {
		_buffer.clear();
		_buffer.limit(4);
		read(_buffer);
		_buffer.flip();
		return _buffer.getInt();
	}

	public float readFloat() throws IOException {
		_buffer.clear();
		_buffer.limit(4);
		read(_buffer);
		_buffer.flip();
		return _buffer.getFloat();
	}

	public double readDouble() throws IOException {
		_buffer.clear();
		_buffer.limit(8);
		read(_buffer);
		_buffer.flip();
		return _buffer.getDouble();
	}

	public String readString() throws IOException {
		final byte[] strData = readByteArray();
		return new String(strData, StandardCharsets.UTF_8);
	}

	public long readLong() throws IOException {
		_buffer.clear();
		_buffer.limit(8);
		read(_buffer);
		_buffer.flip();
		return _buffer.getLong();
	}
	
	public byte[] readByteArray() throws IOException {
		int size = readInt();
		int off = 0;
		final byte[] result = new byte[size];
		while (size > 0) {
			_buffer.clear();
			final int count = Math.min(_buffer.capacity(), size);
			_buffer.limit(count);
			read(_buffer);
			_buffer.flip();
			_buffer.get(result, off, count);
			size -= count;
			off += count;
		}
		return result;
	}
	
	public <T extends IBinarySerializable> T readSerializableClass() throws IOException {
		return readSerializableClass(_defaultCtorArgTypes, _defaultCtorArgs);
	}

	public <T extends IBinarySerializable> T readSerializableClass(final Class<?>[] ctorArgTypes,
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
			final IBinarySerializableId obj) {
		_objectTable.put(identifier, obj);
	}

	public <T extends IBinarySerializableId> T readObjectTable(
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

	public <T extends IBinarySerializableId> T readObjectTable()
			throws IOException {
		return readObjectTable(_defaultCtorArgTypes, _defaultCtorArgs);
	}
	
	public void debugPrint() {
		for (int i = 0; i < _stringTable.size(); i++) {
			System.out.println("str[" + i + "] = " + _stringTable.get(i));
		}
		for (Map.Entry<Long,IBinarySerializableId> entry : _objectTable.entrySet()) {
			System.out.println("obj[" + String.format("%16x", entry.getKey()) + "] = " + entry.getValue().getClass().getName());
		}
	}
	
	public void resetState() {
		_objectTable.clear();
		_stringTable.clear();
	}
}
