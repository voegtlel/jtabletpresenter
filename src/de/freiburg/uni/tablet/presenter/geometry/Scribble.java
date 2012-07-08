package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class Scribble extends AbstractRenderable {
	private final LinkedElementList<ScribbleSegment> _segments = new LinkedElementList<ScribbleSegment>();

	private final IPen _pen;

	public Scribble(final long id, final IPen pen) {
		super(id);
		_pen = pen;
	}

	public Scribble(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_pen = new SolidPen(reader);
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			_segments.addLast(new ScribbleSegment(reader));
		}
	}

	@Override
	public IRenderable cloneRenderable(final long id) {
		final Scribble result = new Scribble(id, _pen);
		for (LinkedElement<ScribbleSegment> segment = _segments.getFirst(); segment != null; segment = segment
				.getNext()) {
			result._segments.addLast(segment.getData().cloneRenderable());
		}
		return result;
	}

	/**
	 * Adds an additional point
	 * 
	 * @param data
	 */
	public void addPoint(final DataPoint data) {
		if (_segments.isEmpty()) {
			_segments.addLast(new ScribbleSegment());
		}
		_segments.getLast().getData().addPoint(data);
	}

	/**
	 * Starts a new segment
	 */
	public void addSegment() {
		if (_segments.getLast() == null
				|| !_segments.getLast().getData().isEmpty()) {
			_segments.addLast(new ScribbleSegment());
		}
	}

	@Override
	public void eraseAt(final EraseInfo eraseInfo) {
		Scribble modifiedObjectInstance = (Scribble) eraseInfo
				.getModifiedObject(this);
		if (modifiedObjectInstance == null) {
			if (collides(eraseInfo.getCollisionInfo())) {
				modifiedObjectInstance = (Scribble) eraseInfo
						.addModifiedObject(this);
			}
		}

		if (modifiedObjectInstance != null) {
			modifiedObjectInstance.eraseAtDirect(eraseInfo.getCollisionInfo());
		}
	}

	@Override
	public void eraseAtDirect(final CollisionInfo collisionInfo) {
		for (LinkedElement<ScribbleSegment> seg = _segments.getFirst(); seg != null;) {
			LinkedElement<ScribbleSegment> nextSeg = seg.getNext();
			final ScribbleSegment newSeg = seg.getData().eraseAt(collisionInfo);
			if (newSeg != null) {
				_segments.insertAfter(seg, newSeg);
				nextSeg = seg.getNext();
			}
			if (seg.getData().isEmpty()) {
				_segments.remove(seg);
			}
			seg = nextSeg;
		}
	}

	@Override
	public boolean collides(final CollisionInfo collisionInfo) {
		for (LinkedElement<ScribbleSegment> seg = _segments.getFirst(); seg != null; seg = seg
				.getNext()) {
			if (seg.getData().collides(collisionInfo)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(final IPageBackRenderer renderer) {
		for (LinkedElement<ScribbleSegment> e = _segments.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().render(_pen, renderer);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		_pen.serialize(writer);
		writer.writeInt(_segments.getFirst().getNextCount());
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			element.getData().serialize(writer);
		}
	}

	/**
	 * Gets the objects pen
	 * 
	 * @return
	 */
	public IPen getPen() {
		return _pen;
	}

	@Override
	public float getMinX() {
		float minX = Float.MAX_VALUE;
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			minX = Math.min(minX, element.getData().getMinX());
		}
		return minX;
	}

	@Override
	public float getMinY() {
		float minY = Float.MAX_VALUE;
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			minY = Math.min(minY, element.getData().getMinY());
		}
		return minY;
	}

	@Override
	public float getMaxX() {
		float maxX = Float.MIN_VALUE;
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			maxX = Math.max(maxX, element.getData().getMaxX());
		}
		return maxX;
	}

	@Override
	public float getMaxY() {
		float maxY = Float.MIN_VALUE;
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			maxY = Math.max(maxY, element.getData().getMaxY());
		}
		return maxY;
	}

	@Override
	public float getRadius() {
		return _pen.getThickness();
	}
}
