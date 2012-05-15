package de.freiburg.uni.tablet.presenter.geometry;

import java.util.Date;

public class DataPoint {
	private final float _x;
	private final float _y;
	private final float _xOrig;
	private final float _yOrig;
	private final long _timestamp;

	public DataPoint(final float x, final float y, final float xOrig,
			final float yOrig) {
		_x = x;
		_y = y;
		_xOrig = xOrig;
		_yOrig = yOrig;
		_timestamp = new Date().getTime();
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
	public float getxOrig() {
		return _xOrig;
	}

	/**
	 * @return the yOrig
	 */
	public float getyOrig() {
		return _yOrig;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return _timestamp;
	}
}
