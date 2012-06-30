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
}
