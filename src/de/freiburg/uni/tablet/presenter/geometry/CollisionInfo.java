/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.geometry;

/**
 * @author lukas
 * 
 */
public class CollisionInfo {
	private final float _x;
	private final float _y;
	private final float _xOrig;
	private final float _yOrig;
	private final float _radiusX;
	private final float _radiusY;
	private final float _radiusXOrig;
	private final float _radiusYOrig;
	private final boolean _checkOnlyBoundaries;

	public CollisionInfo(final float x, final float y, final float xOrig,
			final float yOrig, final float radiusX, final float radiusY,
			final float radiusXOrig, final float radiusYOrig,
			final boolean checkOnlyBoundaries) {
		_x = x;
		_y = y;
		_xOrig = xOrig;
		_yOrig = yOrig;
		_radiusX = radiusX;
		_radiusY = radiusY;
		_radiusXOrig = radiusXOrig;
		_radiusYOrig = radiusYOrig;
		_checkOnlyBoundaries = checkOnlyBoundaries;
	}

	public boolean collides(final float minX, final float minY,
			final float maxX, final float maxY) {
		return minX <= _x + _radiusX && maxX >= _x - _radiusX
				&& minY <= _y + _radiusY && maxY >= _y - _radiusY;
	}

	public boolean collides(final float x, final float y) {
		final float xDiff = x - _x;
		final float yDiff = y - _y;
		return xDiff * xDiff / (_radiusX * _radiusX) + yDiff * yDiff
				/ (_radiusY * _radiusY) <= 1;
	}
	
	public float getCenterX() {
		return _x;
	}
	
	public float getCenterY() {
		return _y;
	}
	
	public float getRadiusX() {
		return _radiusX;
	}
	
	public float getRadiusY() {
		return _radiusY;
	}

	public boolean isCheckOnlyBoundaries() {
		return _checkOnlyBoundaries;
	}
}
