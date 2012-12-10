package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

public class DataPoint implements IBinarySerializable {
	private final float _x;
	private final float _y;
	private final float _xOrig;
	private final float _yOrig;
	private final float _pressure;
	private final long _timestamp;

	public DataPoint(final float x, final float y, final float xOrig,
			final float yOrig, final float pressure, final long timestamp) {
		_x = x;
		_y = y;
		_xOrig = xOrig;
		_yOrig = yOrig;
		_pressure = pressure;
		_timestamp = timestamp;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return _x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return _y;
	}

	/**
	 * @return the xOrig
	 */
	public float getXOrig() {
		return _xOrig;
	}

	/**
	 * @return the yOrig
	 */
	public float getYOrig() {
		return _yOrig;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return _timestamp;
	}

	public DataPoint(final BinaryDeserializer reader) throws IOException {
		_xOrig = reader.readFloat();
		_yOrig = reader.readFloat();
		_x = reader.readFloat();
		_y = reader.readFloat();
		_pressure = reader.readFloat();
		_timestamp = reader.readLong();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeFloat(_xOrig);
		writer.writeFloat(_yOrig);
		writer.writeFloat(_x);
		writer.writeFloat(_y);
		writer.writeFloat(_pressure);
		writer.writeLong(_timestamp);
	}
}
