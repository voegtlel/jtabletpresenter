package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Container;
import java.awt.Cursor;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;

public abstract class AbstractTool implements ITool {
	private boolean _isActive = false;
	private Cursor _cursor = null;
	private final Container _container;

	public AbstractTool(final Container container) {
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
	public void Over(final DataPoint data) {
		_container.setCursor(_cursor);
		_isActive = true;
	}

	@Override
	public void Out(final DataPoint data) {
		_isActive = false;
	}
}
