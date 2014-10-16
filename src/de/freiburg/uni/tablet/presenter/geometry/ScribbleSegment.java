package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
	
	private boolean _hasVariableWidth;

	/**
	 * Creates an empty path segment
	 */
	public ScribbleSegment() {
		_points = new LinkedElementList<DataPoint>();
		_path = new Path2D.Float();
		_hasBoundary = true;
		_hasVariableWidth = false;
		_minX = Float.MAX_VALUE;
		_minY = Float.MAX_VALUE;
		_maxX = Float.MIN_VALUE;
		_maxY = Float.MIN_VALUE;
	}

	private ScribbleSegment(final LinkedElementList<DataPoint> points, final boolean hasVariableWidth) {
		_points = points;
		_hasBoundary = false;
		_hasVariableWidth = hasVariableWidth;
	}

	/**
	 * Adds a point to the painting.
	 * 
	 * @param data
	 *            new point
	 */
	public void addPoint(final DataPoint data) {
		// Check variable width
		if (!_hasVariableWidth) {
			if (!_points.isEmpty() && _points.getFirst().getData().getPressure() != data.getPressure() && _points.getFirst().getData().getPressure() != 0 && data.getPressure() != 0) {
				System.out.println("Var Width");
				_hasVariableWidth = true;
				_path = null;
			}
		}
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
		_hasVariableWidth = false;
		float lastPressure = 0;
		for (LinkedElement<DataPoint> e = _points.getFirst(); e != null; e = e
				.getNext()) {
			_minX = Math.min(_minX, e.getData().getX());
			_minY = Math.min(_minY, e.getData().getY());
			_maxX = Math.max(_maxX, e.getData().getX());
			_maxY = Math.max(_maxY, e.getData().getY());
			if (lastPressure != 0 && lastPressure != e.getData().getPressure() && e.getData().getPressure() != 0) {
				_hasVariableWidth = true;
				_path = null;
			}
			lastPressure = e.getData().getPressure();
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
	private ScribbleSegment eraseAtSimple(final CollisionInfo collisionInfo) {
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
						return new ScribbleSegment(_points.splitAtRemove(e), _hasVariableWidth);
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
	 * Erases points at the location until the line must be splitted. Also splits segments between points.
	 * 
	 * @param collisionInfo
	 *            erase information
	 * @return new element if splitted path, this if modified or null if not modified
	 */
	private ScribbleSegment eraseAtPrecise(final CollisionInfo collisionInfo) {
		if (!_hasBoundary || collisionInfo.collides(_minX, _minY, _maxX, _maxY)) {
			boolean wasModified = false;
			if (_points.hasOne()) {
				if (collisionInfo.collides(_points.getFirst().getData().getX(), _points.getFirst().getData().getY())) {
					_points.clear();
				}
				return this;
			}
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (next != null) {
					Point2D.Float coll1 = new Point2D.Float();
					Point2D.Float coll2 = new Point2D.Float();
					Point2D.Float factors = new Point2D.Float();
					int result = collisionInfo.collidesSegment(e.getData().getX(), e.getData()
							.getY(), next.getData().getX(), next.getData().getY(), coll1, coll2, factors);
					if (result != CollisionInfo.COLLIDE_NONE) {
						wasModified = true;
						_path = null;
						_hasBoundary = false;
						if (collisionInfo.isCheckOnlyBoundaries()) {
							_points.clear();
							break;
						}
					}
					if (result == CollisionInfo.COLLIDE_P1_WITHIN) {
						//System.out.println("Collide P1 within at (" + e.getData().getX() + ", " + e.getData().getY() + "), " + coll2);
						final DataPoint newDataPoint = new DataPoint(coll2.x, coll2.y, collisionInfo.getSourceDataPoint().getXOrig(), collisionInfo.getSourceDataPoint().getYOrig(), e.getData().getPressure() * factors.y + next.getData().getPressure() * (1.0f - factors.y), collisionInfo.getSourceDataPoint().getTimestamp());
						if (e == _points.getFirst()) {
							// Remove the point.
							_points.removeFirst();
							_points.addFirst(newDataPoint);
						} else {
							_points.insertAfter(e, newDataPoint);
							return new ScribbleSegment(_points.splitAtRemove(e), _hasVariableWidth);
						}
					} else if (result == CollisionInfo.COLLIDE_P2_WITHIN) {
						//System.out.println("Collide P2 within at " + coll1 + ", (" + next.getData().getX() + ", " + next.getData().getY() + ")");
						final DataPoint newDataPoint = new DataPoint(coll1.x, coll1.y, collisionInfo.getSourceDataPoint().getXOrig(), collisionInfo.getSourceDataPoint().getYOrig(), e.getData().getPressure() * factors.x + next.getData().getPressure() * (1.0f - factors.x), collisionInfo.getSourceDataPoint().getTimestamp());
						_points.insertAfter(e, newDataPoint);
						if (next == _points.getLast()) {
							_points.removeLast();
							break;
						} else {
							return new ScribbleSegment(_points.splitAt(next), _hasVariableWidth);
						}
					} else if (result == CollisionInfo.COLLIDE_BOTH_WITHIN) {
						//System.out.println("Collide both within at (" + e.getData().getX() + ", " + e.getData().getY() + "), (" + next.getData().getX() + ", " + next.getData().getY() + ")");
						if (e == _points.getFirst()) {
							// Simply remove the point.
							_points.removeFirst();
						} else {
							return new ScribbleSegment(_points.splitAtRemove(e), _hasVariableWidth);
						}
					} else if (result == CollisionInfo.COLLIDE_SEGMENT) {
						//System.out.println("Collide segment within at " + coll1 + ", " + coll2 + ")");
						final DataPoint newDataPoint1 = new DataPoint(coll1.x, coll1.y, collisionInfo.getSourceDataPoint().getXOrig(), collisionInfo.getSourceDataPoint().getYOrig(), e.getData().getPressure() * factors.x + next.getData().getPressure() * (1.0f - factors.x), collisionInfo.getSourceDataPoint().getTimestamp());
						final DataPoint newDataPoint2 = new DataPoint(coll2.x, coll2.y, collisionInfo.getSourceDataPoint().getXOrig(), collisionInfo.getSourceDataPoint().getYOrig(), e.getData().getPressure() * factors.y + next.getData().getPressure() * (1.0f - factors.y), collisionInfo.getSourceDataPoint().getTimestamp());
						_points.insertAfter(e, newDataPoint1);
						_points.insertBefore(next, newDataPoint2);
						final LinkedElement<DataPoint> firstNext = next.getPrevious();
						return new ScribbleSegment(_points.splitAt(firstNext), _hasVariableWidth);
					}
				}
				e = next;
			}
			if (wasModified) {
				return this;
			}
		}
		return null;
	}
	
	/**
	 * Erases points at the location until the line must be splitted.
	 * 
	 * @param collisionInfo
	 *            erase information
	 * @return new element if splitted path, this if modified or null if not modified
	 */
	public ScribbleSegment eraseAt(final CollisionInfo collisionInfo) {
		if (collisionInfo.isFastCollide()) {
			return eraseAtSimple(collisionInfo);
		} else {
			return eraseAtPrecise(collisionInfo);
		}
	}
	
	/**
	 * Replaces the last point by data.
	 * @param data
	 */
	public void updateLastPoint(final DataPoint data) {
		_points.removeLast();
		_points.addLast(data);
		if (_path != null) {
			_path.reset();
			generatePathData();
		}
		// recalc boundary
		_hasBoundary = false;
	}
	
	/**
	 * Checks if the collision info is in range. Checks only vertexes.
	 * 
	 * @param collisionInfo
	 * @return true if in range
	 */
	private boolean collidesSimple(final CollisionInfo collisionInfo) {
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
	 * Checks if the collision info is in range. Checks also for segments to be touched.
	 * 
	 * @param collisionInfo
	 * @return true if in range
	 */
	private boolean collidesPrecise(final CollisionInfo collisionInfo) {
		if (!_hasBoundary || collisionInfo.collides(_minX, _minY, _maxX, _maxY)) {
			if (collisionInfo.isCheckOnlyBoundaries()) {
				return true;
			}
			if (_points.hasOne()) {
				return collisionInfo.collides(_points.getFirst().getData().getX(), _points.getFirst().getData().getY());
			}
			for (LinkedElement<DataPoint> e = _points.getFirst(); e != null;) {
				final LinkedElement<DataPoint> next = e.getNext();
				if (next != null) {
					if (collisionInfo.collidesSegment(e.getData().getX(), e.getData()
							.getY(), next.getData().getX(), next.getData().getY())) {
						return true;
					}
				}
				e = next;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the collision info is in range
	 * 
	 * @param collisionInfo
	 * @return true if in range
	 */
	public boolean collides(final CollisionInfo collisionInfo) {
		if (collisionInfo.isFastCollide()) {
			return collidesSimple(collisionInfo);
		} else {
			return collidesPrecise(collisionInfo);
		}
	}
	
	private void generatePathData() {
		LinkedElement<DataPoint> e = _points.getFirst();
		_path.moveTo(e.getData().getX(), e.getData().getY());
		for (e = e.getNext(); e != null; e = e.getNext()) {
			_path.lineTo(e.getData().getX(), e.getData().getY());
		}
	}

	/**
	 * Creates the graphics object.
	 */
	public void bake() {
		if (!_points.isEmpty() && !_points.hasOne() && !_hasVariableWidth) {
			_path = new Path2D.Float(Path2D.WIND_NON_ZERO, _points.getCount());
			generatePathData();
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
				if (_hasVariableWidth) {
					LinkedElement<DataPoint> e = _points.getFirst();
					LinkedElement<DataPoint> lastP = e;
					renderer.beginPath(pen);
					for (e = e.getNext(); e != null; e = e.getNext()) {
						renderer.drawPath(lastP.getData().getX(), lastP.getData().getY(), e.getData().getX(), e.getData().getY(), e.getData().getPressure());
						lastP = e;
					}
					renderer.endPath();
				} else {
					if (_path == null) {
						bake();
					}
					renderer.draw(pen, _path);
				}
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

		_hasVariableWidth = false;
		float lastPressure = 0;
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final DataPoint e = new DataPoint(reader);
			_points.addLast(e);
			_minX = Math.min(_minX, e.getX());
			_minY = Math.min(_minY, e.getY());
			_maxX = Math.max(_maxX, e.getX());
			_maxY = Math.max(_maxY, e.getY());
			_hasVariableWidth = _hasVariableWidth || (lastPressure != 0 && lastPressure != e.getPressure() && e.getPressure() != 0);
			lastPressure = e.getPressure();
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
		result._hasVariableWidth = _hasVariableWidth;
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
	
	/**
	 * Clones this object
	 * 
	 * @return
	 */
	public ScribbleSegment cloneRenderable(final float offsetX, final float offsetY) {
		final ScribbleSegment result = new ScribbleSegment();
		result._hasBoundary = _hasBoundary;
		result._hasVariableWidth = _hasVariableWidth;
		result._minX = _minX + offsetX;
		result._minY = _minY + offsetY;
		result._maxX = _maxX + offsetX;
		result._maxY = _maxY + offsetY;
		result._path = null;
		/*if (_path != null) {
			result._path = (Path2D) _path.clone();
		}*/
		for (LinkedElement<DataPoint> element = _points.getFirst(); element != null; element = element
				.getNext()) {
			DataPoint offsetPoint = element.getData().clone(offsetX, offsetY);
			result._points.addLast(offsetPoint);
		}
		return result;
	}
}
