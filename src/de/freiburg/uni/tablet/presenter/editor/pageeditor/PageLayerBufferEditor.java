package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public class PageLayerBufferEditor implements IPageLayerBuffer {
	private final PageLayerBufferFront _frontBuffer;
	private final PageLayerBufferBack _backBuffer;

	public PageLayerBufferEditor(final IDisplayRenderer displayRenderer) {
		_frontBuffer = new PageLayerBufferFront(displayRenderer);
		_backBuffer = new PageLayerBufferBack(displayRenderer);
	}

	public PageLayerBufferBack getBackBuffer() {
		return _backBuffer;
	}

	public PageLayerBufferFront getFrontBuffer() {
		return _frontBuffer;
	}

	@Override
	public void resize(final int width, final int height) {
		_frontBuffer.resize(width, height);
		_backBuffer.resize(width, height);
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		_backBuffer.drawBuffer(g, obs);
		_frontBuffer.drawBuffer(g, obs);
	}

	@Override
	public void clear() {
		_frontBuffer.clear();
		_backBuffer.clear();
	}
}
