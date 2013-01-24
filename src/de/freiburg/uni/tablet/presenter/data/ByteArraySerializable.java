package de.freiburg.uni.tablet.presenter.data;

import java.io.IOException;

public class ByteArraySerializable implements IBinarySerializable {
	private byte[] _data;
	private int _offset;
	private int _size;

	public ByteArraySerializable(final byte[] data, final int offset, final int size) {
		_data = data;
		_offset = offset;
		_size = size;
	}
	
	public byte[] getData() {
		return _data;
	}
	
	public int getOffset() {
		return _offset;
	}
	
	public int getSize() {
		return _size;
	}
	
	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeByteArray(_data, _offset, _size);
	}
	
	public ByteArraySerializable(final BinaryDeserializer reader) throws IOException {
		_offset = 0;
		_data = reader.readByteArray();
		_size = _data.length;
	}
}
