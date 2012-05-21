package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

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

	protected BufferedImage createBitmap(final int width, final int height) {
		final Dimension cursorSize = Toolkit.getDefaultToolkit()
				.getBestCursorSize(width, height);
		return new BufferedImage(cursorSize.width, cursorSize.height,
				BufferedImage.TYPE_INT_ARGB);
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
