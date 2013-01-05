/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * @author lukas
 * 
 */
public class BinarySerializer {
	private final DataOutputStream _dataOutputStream;
	private final HashMap<String, Integer> _stringTable = new HashMap<String, Integer>();
	private final HashMap<Long, IBinarySerializable> _objectTable = new HashMap<Long, IBinarySerializable>();

	/**
	 * 
	 */
	public BinarySerializer(final OutputStream stream) {
		_dataOutputStream = new DataOutputStream(stream);
	}

	public void writeBoolean(final boolean value) throws IOException {
		_dataOutputStream.writeBoolean(value);
	}

	public void writeInt(final int value) throws IOException {
		_dataOutputStream.writeInt(value);
	}

	public void writeFloat(final float value) throws IOException {
		_dataOutputStream.writeFloat(value);
	}

	public void writeDouble(final double value) throws IOException {
		_dataOutputStream.writeDouble(value);
	}

	public void writeString(final String value) throws IOException {
		_dataOutputStream.writeUTF(value);
	}

	public void writeLong(final long value) throws IOException {
		_dataOutputStream.writeLong(value);
	}
	
	public void writeByteArray(byte[] data) throws IOException {
		_dataOutputStream.writeInt(data.length);
		_dataOutputStream.write(data);
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

	public void writeObjectTable(final long identifier,
			final IBinarySerializable obj) throws IOException {
		if (obj == null) {
			writeLong(0);
		}
		final Object data = _objectTable.get(identifier);
		if (data != null) {
			writeLong(identifier);
		} else {
			_objectTable.put(identifier, obj);
			writeLong(identifier | 0x8000000000000000l);
			writeSerializableClass(obj);
		}
	}
}
