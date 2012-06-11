package de.freiburg.uni.tablet.presenter.geometry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class Scribble implements IRenderable {
	private final LinkedElementList<ScribbleSegment> _segments = new LinkedElementList<ScribbleSegment>();

	private final IPen _pen;

	public Scribble(final IPen pen) {
		_pen = pen;
	}

	public Scribble(final DataInputStream reader) throws IOException {
		_pen = new SolidPen(reader);
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			_segments.addLast(new ScribbleSegment(reader));
		}
	}

	public void addPoint(final DataPoint data) {
		if (_segments.isEmpty()) {
			_segments.addLast(new ScribbleSegment());
		}
		_segments.getLast().getData().addPoint(data);
	}

	public void addSegment() {
		if ((_segments.getLast() == null)
				|| !_segments.getLast().getData().isEmpty()) {
			_segments.addLast(new ScribbleSegment());
		}
	}

	@Override
	public void eraseAt(final EraseInfo eraseInfo) {
		for (LinkedElement<ScribbleSegment> seg = _segments.getFirst(); seg != null;) {
			LinkedElement<ScribbleSegment> nextSeg = seg.getNext();
			final ScribbleSegment newSeg = seg.getData().eraseAt(eraseInfo);
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
	public void render(final IPageRenderer renderer) {
		for (LinkedElement<ScribbleSegment> e = _segments.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().render(_pen, renderer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.freiburg.uni.tablet.presenter.IBinarySerializable#serialize(java.io
	 * .DataOutputStream)
	 */
	@Override
	public void serialize(final DataOutputStream writer) throws IOException {
		_pen.serialize(writer);
		writer.writeInt(_segments.getFirst().getNextCount());
		for (LinkedElement<ScribbleSegment> element = _segments.getFirst(); element != null; element = element
				.getNext()) {
			element.getData().serialize(writer);
		}
	}
}
