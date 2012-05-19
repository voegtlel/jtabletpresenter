package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class Scribble implements IRenderable {
	private final LinkedElementList<ScribbleSegment> _segments = new LinkedElementList<ScribbleSegment>();

	private final IPen _pen;

	public Scribble(final IPen pen) {
		_pen = pen;
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
}
