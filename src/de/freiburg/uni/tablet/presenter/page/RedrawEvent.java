package de.freiburg.uni.tablet.presenter.page;

import java.util.EventObject;

public class RedrawEvent extends EventObject {

	private final IPageRenderer _renderer;

	public RedrawEvent(final Object source, final IPageRenderer renderer) {
		super(source);
		_renderer = renderer;
	}

	public IPageRenderer getRenderer() {
		return _renderer;
	}

}
