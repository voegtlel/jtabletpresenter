package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;

public abstract class AbstractTool implements ITool {
	private boolean _isActive = false;
	private Cursor _cursor = null;
	private final IToolContainer _container;

	public AbstractTool(final IToolContainer container) {
		_container = container;
		_cursor = generateCursor();
	}

	protected abstract Cursor generateCursor();

	protected void updateCursor() {
		_cursor = generateCursor();
		if (_isActive) {
			_container.setCursor(_cursor);
		}
	}

	@Override
	public void over() {
		_container.setCursor(_cursor);
		_isActive = true;
	}

	@Override
	public void out() {
		_isActive = false;
	}
}
