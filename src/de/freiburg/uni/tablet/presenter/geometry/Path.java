package de.freiburg.uni.tablet.presenter.geometry;

import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class Path implements IRenderable {
	private final LinkedElementList<PathSegment> _segments = new LinkedElementList<PathSegment>();

	private final IPen _pen;

	public Path(final IPen pen) {
		_pen = pen;
	}

	public void addPoint(final DataPoint data) {
		if (_segments.isEmpty()) {
			_segments.addLast(new PathSegment());
		}
		_segments.getLast().getData().addPoint(data);
	}

	public void addSegment() {
		if ((_segments.getLast() == null)
				|| !_segments.getLast().getData().isEmpty()) {
			_segments.addLast(new PathSegment());
		}
	}

	public void eraseAt(final EraseInfo eraseInfo) {
		for (LinkedElement<PathSegment> seg = _segments.getFirst(); seg != null;) {
			LinkedElement<PathSegment> nextSeg = seg.getNext();
			final PathSegment newSeg = seg.getData().eraseAt(eraseInfo);
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
		for (LinkedElement<PathSegment> e = _segments.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().render(_pen, renderer);
		}
	}
}
