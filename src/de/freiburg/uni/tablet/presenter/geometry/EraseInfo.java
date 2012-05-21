package de.freiburg.uni.tablet.presenter.geometry;

public class EraseInfo {
	private final float _x;
	private final float _y;
	private final float _radiusX;
	private final float _radiusY;

	public EraseInfo(final float x, final float y, final float radiusX,
			final float radiusY) {
		_x = x;
		_y = y;
		_radiusX = radiusX;
		_radiusY = radiusY;
	}

	public boolean isInRange(final float minX, final float minY,
			final float maxX, final float maxY) {
		return (minX <= (_x + _radiusX)) && (maxX >= (_x - _radiusX))
				&& (minY <= (_y + _radiusY)) && (maxY >= (_y - _radiusY));
	}

	public boolean isInRadius(final float x, final float y) {
		final float xDiff = x - _x;
		final float yDiff = y - _y;
		return ((((xDiff * xDiff) / (_radiusX * _radiusX)) + ((yDiff * yDiff) / (_radiusY * _radiusY))) <= 1);
	}
}
