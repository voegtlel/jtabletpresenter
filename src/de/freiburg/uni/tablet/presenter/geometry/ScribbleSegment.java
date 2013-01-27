package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
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
	 * @param collisionInfo
	 *            erase information
	 * @return new element if splitted path, this if modified or null if not modified
	 */
	public ScribbleSegment eraseAt(final CollisionInfo collisionInfo) {
		if (!_hasBoundary || collisionInfo.collides(_minX, _minY, _maxX, _maxY)) {
			boolean wasModified = false;
			_minX = Float.MAX_VALUE;
			_minY = Float.MAX_VALUE;
			_maxX = Float.MIN_VALUE;
			_maxY = Float.MIN_VALUE;
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (collisionInfo.collides(e.getData().getX(), e.getData()
						.getY())) {
					wasModified = true;
					if (collisionInfo.isCheckOnlyBoundaries()) {
						while (!_points.isEmpty()) {
							_points.removeFirst();
						}
						break;
					}
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
			if (wasModified) {
				return this;
			}
		}
		return null;
	}

	/**
	 * Checks if the collision info is in range
	 * 
	 * @param collisionInfo
	 * @return true if in range
	 */
	public boolean collides(final CollisionInfo collisionInfo) {
		if (!_hasBoundary || collisionInfo.collides(_minX, _minY, _maxX, _maxY)) {
			if (collisionInfo.isCheckOnlyBoundaries()) {
				return true;
			}
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (collisionInfo.collides(e.getData().getX(), e.getData()
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
			_path = new Path2D.Float(Path2D.WIND_NON_ZERO, _points.getCount());
			LinkedElement<DataPoint> e = _points.getFirst();
			_path.moveTo(e.getData().getX(), e.getData().getY());
			for (e = e.getNext(); e != null; e = e.getNext()) {
				_path.lineTo(e.getData().getX(), e.getData().getY());
			}
		}
	}

	public float getMinX() {
		if (!_hasBoundary) {
			calcBoundary();
		}
		return _minX;
	}

	public float getMinY() {
		if (!_hasBoundary) {
			calcBoundary();
		}
		return _minY;
	}

	public float getMaxX() {
		if (!_hasBoundary) {
			calcBoundary();
		}
		return _maxX;
	}

	public float getMaxY() {
		if (!_hasBoundary) {
			calcBoundary();
		}
		return _maxY;
	}

	/**
	 * Renders the segment
	 * 
	 * @param pen
	 * @param renderer
	 */
	public void render(final IPen pen, final IPageBackRenderer renderer) {
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

	/**
	 * Returns true, if this segment has no data
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return _points.isEmpty();
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

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_points.getCount());

		for (LinkedElement<DataPoint> element = _points.getFirst(); element != null; element = element
				.getNext()) {
			element.getData().serialize(writer);
		}
	}

	/**
	 * Clones this object
	 * 
	 * @return
	 */
	public ScribbleSegment cloneRenderable() {
		final ScribbleSegment result = new ScribbleSegment();
		result._hasBoundary = _hasBoundary;
		result._minX = _minX;
		result._minY = _minY;
		result._maxX = _maxX;
		result._maxY = _maxY;
		result._path = null;
		/*if (_path != null) {
			result._path = (Path2D) _path.clone();
		}*/
		for (LinkedElement<DataPoint> element = _points.getFirst(); element != null; element = element
				.getNext()) {
			result._points.addLast(element.getData());
		}
		return result;
	}
}
