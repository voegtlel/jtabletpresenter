package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;

public abstract class AbstractTool implements ITool, IPageRepaintListener {
	private boolean _isActive = false;
	private Cursor _cursor = null;
	protected final IToolPageEditor _editor;

	public AbstractTool(final IToolPageEditor editor) {
		_editor = editor;
	}

	@Override
	public void invalidateCursor() {
		_cursor = null;
		if (_isActive) {
			updateCursor();
		}
	}

	protected abstract Cursor generateCursor();
	
	@Override
	public void drawAlways(final DataPoint data) {
	}

	protected void updateCursor() {
		_cursor = generateCursor();
		if (_isActive) {
			_editor.getPageEditor().setCursor(_cursor);
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
		if (_cursor == null) {
			updateCursor();
		}
		_editor.getPageEditor().setCursor(_cursor);
		_isActive = true;
	}

	@Override
	public void out() {
		_isActive = false;
	}
	
	@Override
	public void updateTool() {
	}
}
