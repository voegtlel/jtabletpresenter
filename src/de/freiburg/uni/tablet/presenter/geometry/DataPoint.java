package de.freiburg.uni.tablet.presenter.geometry;


public class DataPoint {
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
}
