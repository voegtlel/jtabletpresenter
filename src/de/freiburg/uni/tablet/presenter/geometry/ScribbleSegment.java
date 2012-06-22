package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class ScribbleSegment implements IBinarySerializable {
	private Path2D _path;

	private boolean _hasBoundary;
	private float _minX;
	private float _minY;
	private float _maxX;
	private float _maxY;

	private final LinkedElementList<DataPoint> _points;

	/**
	 * Creates an empty path segment
	 */
	public ScribbleSegment() {
		_points = new LinkedElementList<DataPoint>();
		_path = new Path2D.Float();
		_hasBoundary = true;
		_minX = Float.MAX_VALUE;
		_minY = Float.MAX_VALUE;
		_maxX = Float.MIN_VALUE;
		_maxY = Float.MIN_VALUE;
	}

	public ScribbleSegment(final BinaryDeserializer reader) throws IOException {
		_points = new LinkedElementList<DataPoint>();
		_minX = Float.MAX_VALUE;
		_minY = Float.MAX_VALUE;
		_maxX = Float.MIN_VALUE;
		_maxY = Float.MIN_VALUE;
		_hasBoundary = true;

		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final DataPoint e = new DataPoint(reader);
			_points.addLast(e);
			_minX = Math.min(_minX, e.getX());
			_minY = Math.min(_minY, e.getY());
			_maxX = Math.max(_maxX, e.getX());
			_maxY = Math.max(_maxY, e.getY());
		}
	}

	private ScribbleSegment(final LinkedElementList<DataPoint> points) {
		_points = points;
		_hasBoundary = false;
	}

	/**
	 * Adds a point to the painting.
	 * 
	 * @param data
	 *            new point
	 */
	public void addPoint(final DataPoint data) {
		// Add
		_points.addFirst(data);
		// Check for baking object
		if (_path != null) {
			if (_points.hasOne()) {
				// First point
				_path.moveTo(data.getX(), data.getY());
			} else {
				_path.lineTo(data.getX(), data.getY());
			}
		}
		// fix boundary
		if (_hasBoundary) {
			_minX = Math.min(_minX, data.getX());
			_maxX = Math.max(_maxX, data.getX());
			_minY = Math.min(_minY, data.getY());
			_maxY = Math.max(_maxY, data.getY());
		}
	}

	/**
	 * Calculates the boundary
	 */
	public void calcBoundary() {
		_minX = Float.MAX_VALUE;
		_minY = Float.MAX_VALUE;
		_maxX = Float.MIN_VALUE;
		_maxY = Float.MIN_VALUE;
		for (LinkedElement<DataPoint> e = _points.getFirst(); e != null; e = e
				.getNext()) {
			_minX = Math.min(_minX, e.getData().getX());
			_minY = Math.min(_minY, e.getData().getY());
			_maxX = Math.max(_maxX, e.getData().getX());
			_maxY = Math.max(_maxY, e.getData().getY());
		}
		_hasBoundary = true;
	}

	/**
	 * Erases points at the location until the line must be splitted.
	 * Additionally calculates the boundaries.
	 * 
	 * @param eraseInfo
	 *            erase information
	 * @return splitted path or null, if not splitted
	 */
	public ScribbleSegment eraseAt(final EraseInfo eraseInfo) {
		if (!_hasBoundary || eraseInfo.isInRange(_minX, _minY, _maxX, _maxY)) {
			_minX = Float.MAX_VALUE;
			_minY = Float.MAX_VALUE;
			_maxX = Float.MIN_VALUE;
			_maxY = Float.MIN_VALUE;
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (eraseInfo
						.isInRadius(e.getData().getX(), e.getData().getY())) {
					if (e == _points.getFirst()) {
						// Simply remove the point.
						_points.removeFirst();
						_path = null;
					} else if (e == _points.getLast()) {
						// Simply remove the point. Do not return.
						_points.removeLast();
						_path = null;
					} else {
						_path = null;
						_hasBoundary = true;
						return new ScribbleSegment(_points.splitAtRemove(e));
					}
				} else {
					_minX = Math.min(_minX, e.getData().getX());
					_minY = Math.min(_minY, e.getData().getY());
					_maxX = Math.max(_maxX, e.getData().getX());
					_maxY = Math.max(_maxY, e.getData().getY());
				}
				e = next;
			}
			_hasBoundary = true;
		}
		return null;
	}

	/**
	 * Checks if the collision info is in range
	 * 
	 * @param collisionInfo
	 * @return true if in range
	 */
	public boolean isInRange(final CollisionInfo collisionInfo) {
		if (!_hasBoundary
				|| collisionInfo.isInRange(_minX, _minY, _maxX, _maxY)) {
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (collisionInfo.isInRadius(e.getData().getX(), e.getData()
						.getY())) {
					return true;
				}
				e = next;
			}
		}
		return false;
	}

	/**
	 * Creates the graphics object.
	 */
	public void bake() {
		if (!_points.isEmpty() && !_points.hasOne()) {
			_path = new Path2D.Float(Path2D.WIND_NON_ZERO, _points.getFirst()
					.getNextCount());
			LinkedElement<DataPoint> e = _points.getFirst();
			_path.moveTo(e.getData().getX(), e.getData().getY());
			for (e = e.getNext(); e != null; e = e.getNext()) {
				_path.lineTo(e.getData().getX(), e.getData().getY());
			}
		}
	}

	/**
	 * Renders the segment
	 * 
	 * @param pen
	 * @param renderer
	 */
	public void render(final IPen pen, final IPageRenderer renderer) {
		if (!_points.isEmpty()) {
			if (_points.hasOne()) {
				renderer.draw(pen, _points.getFirst().getData().getX(), _points
						.getFirst().getData().getY());
			} else {
				if (_path == null) {
					bake();
				}
				renderer.draw(pen, _path);
			}
		}
	}

	public boolean isEmpty() {
		return _points.isEmpty();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_points.getFirst().getNextCount());

		for (LinkedElement<DataPoint> element = _points.getFirst(); element != null; element = element
				.getNext()) {
			element.getData().serialize(writer);
		}
	}
}
