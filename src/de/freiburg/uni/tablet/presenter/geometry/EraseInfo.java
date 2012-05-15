package de.freiburg.uni.tablet.presenter.geometry;

public class EraseInfo {
	private float _radiusX;
	private float _radiusY;
	private float _x;
	private float _y;

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
