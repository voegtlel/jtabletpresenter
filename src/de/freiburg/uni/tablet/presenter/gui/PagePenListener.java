package de.freiburg.uni.tablet.presenter.gui;

import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PKindEvent;
import jpen.PLevel.Type;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.event.PenListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class PagePenListener implements PenListener {
	private ITool _normalTool;
	private ITool _invertedTool;

	public PagePenListener() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void penButtonEvent(final PButtonEvent e) {
	}

	@Override
	public void penLevelEvent(final PLevelEvent e) {
		if (!e.isMovement()) {
			return;
		}
		final DataPoint dp = new DataPoint(e.pen.getLevelValue(Type.X),
				e.pen.getLevelValue(Type.Y), e.pen.getLevelValue(Type.X),
				e.pen.getLevelValue(Type.Y),
				e.pen.getLevelValue(Type.PRESSURE), e.getTime());

		if (e.pen.getKind() == PKind.valueOf(PKind.Type.ERASER)) {
			_invertedTool.Draw(dp);
		} else {
			_normalTool.Draw(dp);
		}
	}

	@Override
	public void penKindEvent(final PKindEvent ev) {
	}

	@Override
	public void penScrollEvent(final PScrollEvent ev) {
	}

	@Override
	public void penTock(final long availableMillis) {
		// TODO: redraw on change
	}
}
