package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.Color;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class Scribble extends AbstractRenderable {
	
	private final LinkedElementList<ScribbleSegment> _segments = new LinkedElementList<ScribbleSegment>();

	private final IPen _pen;

	public Scribble(final DocumentPage parent, final IPen pen) {
		super(parent);
		_pen = pen;
	}

	@Override
	public IRenderable cloneRenderable(final DocumentPage parent) {
		final Scribble result = new Scribble(parent, _pen);
		for (LinkedElement<ScribbleSegment> segment = _segments.getFirst(); segment != null; segment = segment
				.getNext()) {
			result._segments.addLast(segment.getData().cloneRenderable());
		}
		return result;
	}
	
	@Override
	public IRenderable cloneRenderable(final DocumentPage parent, final float offsetX, final float offsetY) {
		final Scribble result = new Scribble(parent, _pen);
		for (LinkedElement<ScribbleSegment> segment = _segments.getFirst(); segment != null; segment = segment
				.getNext()) {
			result._segments.addLast(segment.getData().cloneRenderable(offsetX, offsetY));
		}
		return result;
	}
	
	/**
	 * Replaces the last added point by data
	 * @param data
	 */
	public void updateLastPoint(final DataPoint data) {
		_segments.getLast().getData().updateLastPoint(data);
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
		_parent.fireRenderableModified(this);
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
	public float getRadius(final RenderMetric metric) {
		return _pen.getThickness(metric, IPen.MAX_PRESSURE);
	}
	
	
	@Override
	synchronized public boolean eraseStart(final EraseInfo eraseInfo) {
		return true;
	}
	
	@Override
	synchronized public boolean eraseAt(final EraseInfo eraseInfo) {
		_parent.fireRenderableModifying(this);
		boolean wasModified = false;
		for (LinkedElement<ScribbleSegment> seg = _segments.getFirst(); seg != null;) {
			LinkedElement<ScribbleSegment> nextSeg = seg.getNext();
			final ScribbleSegment newSeg = seg.getData().eraseAt(eraseInfo.getCollisionInfo());
			if (newSeg != null) {
				wasModified = true;
				if (newSeg != seg.getData()) {
					if (eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
						_segments.remove(seg);
					} else {
						_segments.insertAfter(seg, newSeg);
						nextSeg = seg.getNext();
					}
				}
			}
			if (seg.getData().isEmpty()) {
				_segments.remove(seg);
			}
			seg = nextSeg;
		}
		if (wasModified) {
			_parent.fireRenderableModified(this);
		}
		return !_segments.isEmpty();
	}
	
	
	@Override
	synchronized public boolean eraseEnd(final EraseInfo eraseInfo) {
		if (!_segments.isEmpty()) {
			_parent.fireRenderableModifyEnd(this);
		}
		return !_segments.isEmpty();
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
	synchronized public void render(final IPageBackRenderer renderer) {
		for (LinkedElement<ScribbleSegment> e = _segments.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().render(_pen, renderer);
		}
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		final IPen highlightPen = new SolidPen(_pen.getThickness() + 4, Color.yellow);
		for (LinkedElement<ScribbleSegment> e = _segments.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().render(highlightPen, renderer);
		}
	}
	
	public Scribble(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_pen = reader.readSerializableClass();
		deserializeData(reader);
	}

	@Override
	synchronized public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeSerializableClass(_pen);
		// Always has first
		serializeData(writer);
	}
	
	@Override
	synchronized public void deserializeData(final BinaryDeserializer reader) throws IOException {
		_parent.fireRenderableModifying(this);
		final int count = reader.readInt();
		_segments.clear();
		for (int i = 0; i < count; i++) {
			_segments.addLast(new ScribbleSegment(reader));
		}
		_parent.fireRenderableModified(this);
		_parent.fireRenderableModifyEnd(this);
	}
	
	@Override
	synchronized public void serializeData(final BinarySerializer writer) throws IOException {
		writer.writeInt(_segments.getCount());
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			element.getData().serialize(writer);
		}
	}
}
