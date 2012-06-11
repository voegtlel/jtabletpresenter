/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * 
 */
public class BinarySerializer {
	private final DataOutputStream _dataOutputStream;
	private final List<String> _stringTable = new ArrayList<String>();

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
		final int index = _stringTable.indexOf(value);
		if (index != -1) {
			writeBoolean(true);
			writeInt(index);
		} else {
			writeBoolean(false);
			writeString(value);
			_stringTable.add(value);
		}
	}
}
