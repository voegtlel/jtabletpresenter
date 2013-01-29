/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author lukas
 * 
 */
public class BinarySerializer {
	private final ByteBuffer _buffer = ByteBuffer.allocateDirect(128);
	private final WritableByteChannel _channel;
	private final HashMap<String, Integer> _stringTable = new HashMap<String, Integer>();
	private final HashMap<Long, IBinarySerializableId> _objectTable = new HashMap<Long, IBinarySerializableId>();

	/**
	 * 
	 */
	public BinarySerializer(final WritableByteChannel channel) {
		_channel = channel;
	}
	
	public void write(final ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			final int res = _channel.write(buffer);
			if (res == -1) {
				throw new EOFException();
			}
		}
	}

	public void writeBoolean(final boolean value) throws IOException {
		_buffer.clear();
		_buffer.put(value?(byte)255:(byte)0);
		_buffer.flip();
		write(_buffer);
	}

	public void writeInt(final int value) throws IOException {
		_buffer.clear();
		_buffer.putInt(value);
		_buffer.flip();
		write(_buffer);
	}

	public void writeFloat(final float value) throws IOException {
		_buffer.clear();
		_buffer.putFloat(value);
		_buffer.flip();
		write(_buffer);
	}

	public void writeDouble(final double value) throws IOException {
		_buffer.clear();
		_buffer.putDouble(value);
		_buffer.flip();
		write(_buffer);
	}

	public void writeString(final String value) throws IOException {
		_buffer.clear();
		final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		writeByteArray(bytes, 0, bytes.length);
	}

	public void writeLong(final long value) throws IOException {
		_buffer.clear();
		_buffer.putLong(value);
		_buffer.flip();
		write(_buffer);
	}
	
	public void writeByteArray(final byte[] data, final int offset, final int length) throws IOException {
		_buffer.clear();
		_buffer.putInt(length);
		int off = offset;
		int len = length;
		while (len > 0) {
			final int write = Math.min(_buffer.remaining(), len);
			_buffer.put(data, off, write);
			_buffer.flip();
			write(_buffer);
			len -= write;
			off += write;
			_buffer.clear();
		}
	}

	public void writeSerializableClass(final IBinarySerializable serializable)
			throws IOException {
		writeStringTable(serializable.getClass().getName());
		serializable.serialize(this);
	}

	public void writeStringTable(final String value) throws IOException {
		final Integer index = _stringTable.get(value);
		if (index != null) {
			writeInt(index);
		} else {
			final int newIndex = _stringTable.size();
			_stringTable.put(value, newIndex);
			writeInt(newIndex | 0x80000000);
			writeString(value);
		}
	}
	
	public void putObjectTable(final IBinarySerializableId obj) throws IOException {
		_objectTable.put(obj.getId(), obj);
	}

	public void writeObjectTable(final IBinarySerializableId obj) throws IOException {
		if (obj == null) {
			writeLong(0);
			return;
		}
		final Object data = _objectTable.get(obj.getId());
		if (data != null) {
			if (data != obj) {
				throw new IllegalStateException("Object table out of sync");
			}
			writeLong(obj.getId());
		} else {
			_objectTable.put(obj.getId(), obj);
			writeLong(obj.getId() | 0x8000000000000000l);
			writeSerializableClass(obj);
		}
	}
	
	/*public void debugPrint() {
		for (Map.Entry<String, Integer> entry : _stringTable.entrySet()) {
			System.out.println("str[" + entry.getKey() + "] = " + entry.getValue());
		}
		for (Map.Entry<Long,IBinarySerializableId> entry : _objectTable.entrySet()) {
			System.out.println("obj[" + String.format("%16x", entry.getKey()) + "] = " + entry.getValue().getClass().getName());
		}
	}*/
	
	/**
	 * Resets the object table.
	 */
	public void resetState() {
		_objectTable.clear();
		_stringTable.clear();
	}

	public void flush() throws IOException {
		if (_channel instanceof Flushable) {
			((Flushable) _channel).flush();
		}
	}
}
