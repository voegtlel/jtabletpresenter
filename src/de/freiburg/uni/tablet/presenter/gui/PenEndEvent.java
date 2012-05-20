package de.freiburg.uni.tablet.presenter.gui;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class PenEndEvent extends PenEvent {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private final IRenderable _result;

	public PenEndEvent(final Object source, final ITool activeTool,
			final IRenderable result) {
		super(source, activeTool);
		_result = result;
	}

	public IRenderable getResult() {
		return _result;
	}
}
