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
import java.util.Map;

/**
 * @author lukas
 * 
 */
public class BinarySerializer {
	private final ByteBuffer _buffer = ByteBuffer.allocateDirect(1024);
	private final WritableByteChannel _channel;
	private final HashMap<String, Integer> _stringTable = new HashMap<String, Integer>();
	private final HashMap<Long, IBinarySerializableId> _objectTable = new HashMap<Long, IBinarySerializableId>();
	
	private boolean _autoFlush = false;

	/**
	 * 
	 */
	public BinarySerializer(final WritableByteChannel channel) {
		_channel = channel;
	}
	
	public void flush() throws IOException {
		_buffer.flip();
		while (_buffer.hasRemaining()) {
			final int res = _channel.write(_buffer);
			if (res == -1) {
				throw new EOFException();
			}
		}
		_buffer.clear();
		if (_channel instanceof Flushable) {
			((Flushable) _channel).flush();
		}
	}
	
	private void checkFlush(final int requiredSize) throws IOException {
		if (_buffer.remaining() < requiredSize) {
			flush();
		}
	}
	
	private void write() throws IOException {
		if (_autoFlush) {
			flush();
		}
	}

	public void writeBoolean(final boolean value) throws IOException {
		checkFlush(1);
		_buffer.put(value?(byte)255:(byte)0);
		write();
	}

	public void writeInt(final int value) throws IOException {
		checkFlush(4);
		_buffer.putInt(value);
		write();
	}

	public void writeFloat(final float value) throws IOException {
		checkFlush(4);
		_buffer.putFloat(value);
		write();
	}

	public void writeDouble(final double value) throws IOException {
		checkFlush(8);
		_buffer.putDouble(value);
		write();
	}

	public void writeString(final String value) throws IOException {
		final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		writeByteArray(bytes, 0, bytes.length);
	}

	public void writeLong(final long value) throws IOException {
		checkFlush(8);
		_buffer.putLong(value);
		write();
	}
	
	public void writeByteArray(final byte[] data, final int offset, final int length) throws IOException {
		checkFlush(4);
		_buffer.putInt(length);
		int off = offset;
		int len = length;
		while (len > 0) {
			final int write = Math.min(_buffer.remaining(), len);
			checkFlush(write);
			_buffer.put(data, off, write);
			write();
			len -= write;
			off += write;
			checkFlush(len);
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
				throw new IllegalStateException("Object table out of sync: Id " + String.format("%16X", obj.getId()) + " -> " + data.getClass().getName() + " <-> " + obj.getClass().getName());
			}
			writeLong(obj.getId());
		} else {
			_objectTable.put(obj.getId(), obj);
			writeLong(obj.getId() | 0x8000000000000000l);
			writeSerializableClass(obj);
		}
	}
	
	public void debugPrint() {
		for (Map.Entry<String, Integer> entry : _stringTable.entrySet()) {
			System.out.println("str[" + entry.getKey() + "] = " + entry.getValue());
		}
		for (Map.Entry<Long,IBinarySerializableId> entry : _objectTable.entrySet()) {
			System.out.println("obj[" + String.format("%16x", entry.getKey()) + "] = " + entry.getValue().getClass().getName());
		}
	}
	
	/**
	 * Resets the object table.
	 */
	public void resetState() {
		_objectTable.clear();
		_stringTable.clear();
	}
}
