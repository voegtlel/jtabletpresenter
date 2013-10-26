/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Point2D;

/**
 * @author lukas
 * 
 */
public class CollisionInfo {
	private static final float DELTA = 0.0001f;
	
	private final float _x;
	private final float _y;
	private final float _xOrig;
	private final float _yOrig;
	private final float _radiusX;
	private final float _radiusY;
	private final float _radiusXOrig;
	private final float _radiusYOrig;
	private final boolean _checkOnlyBoundaries;
	private final boolean _fastCollide;
	
	private final DataPoint _sourceDataPoint;

	public CollisionInfo(final float x, final float y, final float xOrig,
			final float yOrig, final float radiusX, final float radiusY,
			final float radiusXOrig, final float radiusYOrig,
			final boolean checkOnlyBoundaries, final DataPoint sourceDataPoint, final boolean fastCollide) {
		_x = x;
		_y = y;
		_xOrig = xOrig;
		_yOrig = yOrig;
		_radiusX = radiusX;
		_radiusY = radiusY;
		_radiusXOrig = radiusXOrig;
		_radiusYOrig = radiusYOrig;
		_checkOnlyBoundaries = checkOnlyBoundaries;
		_sourceDataPoint = sourceDataPoint;
		_fastCollide = fastCollide;
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
	
	private Point2D.Float distPointLineSquared(final float x1, final float y1, final float x2, final float y2, final float x, final float y) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float wx = x - x1;
		float wy = y - y1;
		float dot1 = (wx*dx + wy*dy);
		if (dot1 <= 0) {
			return new Point2D.Float(wx, wy);
		}
		float distSquared = (dx*dx+dy*dy);
		if (distSquared <= dot1) {
			return new Point2D.Float(x-x2, y-y2);
		}
		float b = dot1 / distSquared;
		return new Point2D.Float(x-(x1 + b * dx), y-(y1 + b * dy));
	}
	
	public static final int COLLIDE_NONE = 0;
	public static final int COLLIDE_P1_WITHIN = 1;
	public static final int COLLIDE_P2_WITHIN = 2;
	public static final int COLLIDE_BOTH_WITHIN = 3;
	public static final int COLLIDE_SEGMENT = 4;
	
	private int collideCircle(final float x, final float y, final float r, final float x1, final float y1, final float x2, final float y2, final Point2D.Float p1, final Point2D.Float p2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		
		float wx = x1 - x;
		float wy = y1 - y;
		
		float ldsq = (dx*dx + dy*dy);
		if (Math.abs(ldsq) < DELTA * DELTA) {
			return (wx*wx + wy*wy < r*r)?COLLIDE_BOTH_WITHIN:COLLIDE_NONE;
		}
		
		float c1 = (wx*dx + wy*dy) / ldsq;
		float c2 = (wx*wx + wy*wy - r*r) / ldsq;
		float c3 = c1*c1 - c2;
		if (c3 <= 0) {
			return COLLIDE_NONE;
		}
		float c3sqrt = (float)Math.sqrt(c3);
		float f1 = -c1 - c3sqrt;
		float f2 = -c1 + c3sqrt;
		if (((f1 <= 0) && (f2 <= 0))
				|| ((f1 >= 1) && (f2 >= 1))) {
			return COLLIDE_NONE;
		}
		
		if (f1 > f2) {
			throw new IllegalStateException("f1 > f2");
		}
		
		p1.x = x1 + f1 * dx;
		p1.y = y1 + f1 * dy;
		p2.x = x1 + f2 * dx;
		p2.y = y1 + f2 * dy;
		
		if ((f1 >= 0) && (f2 <= 1)) {
			return COLLIDE_SEGMENT;
		} else if ((f1 <= 0) && (f2 >= 1)) {
			return COLLIDE_BOTH_WITHIN;
		} else if (f1 <= 0) {
			return COLLIDE_P1_WITHIN;
		} else if (f2 >= 1) {
			return COLLIDE_P2_WITHIN;
		}
		throw new IllegalStateException("Invalid case");
	}
	
	public boolean collidesSegment(final float x1, final float y1, final float x2, final float y2) {
		if (!collides(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2))) {
			return false;
		}
		Point2D.Float dist = distPointLineSquared(x1, y1, x2, y2, _x, _y);
		return dist.x * dist.x / (_radiusX * _radiusX) + dist.y * dist.y
				/ (_radiusY * _radiusY) <= 1;
	}
	
	public int collidesSegment(final float x1, final float y1, final float x2, final float y2, final Point2D.Float coll1, final Point2D.Float coll2) {
		float xFactor = _radiusY / _radiusX;
		//float xFactor = 1.0f;
		int result = collideCircle(_x * xFactor, _y, _radiusY, x1 * xFactor, y1, x2 * xFactor, y2, coll1, coll2);
		coll1.x /= xFactor;
		coll2.x /= xFactor;
		return result;
		
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

	public DataPoint getSourceDataPoint() {
		return _sourceDataPoint;
	}

	public boolean isFastCollide() {
		return _fastCollide;
	}
}
