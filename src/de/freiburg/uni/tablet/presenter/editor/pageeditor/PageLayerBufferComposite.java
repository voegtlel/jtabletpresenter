package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class PageLayerBufferComposite implements IPageLayerBuffer {
	private final IDisplayRenderer _displayRenderer;

	private final ArrayList<IPageLayerBuffer> _pageLayerBuffers = new ArrayList<IPageLayerBuffer>();

	public PageLayerBufferComposite(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}

	public PageLayerBufferFront addFrontBuffer() {
		final PageLayerBufferFront result = new PageLayerBufferFront(
				_displayRenderer);
		_pageLayerBuffers.add(result);
		return result;
	}

	public PageLayerBufferBack addBackBuffer() {
		final PageLayerBufferBack result = new PageLayerBufferBack(
				_displayRenderer);
		_pageLayerBuffers.add(result);
		return result;
	}

	public PageLayerBufferComposite addComposite() {
		final PageLayerBufferComposite result = new PageLayerBufferComposite(
				_displayRenderer);
		_pageLayerBuffers.add(result);
		return result;
	}

	public void removeBuffer(final IPageLayerBuffer buffer) {
		_pageLayerBuffers.remove(buffer);
	}

	@Override
	public void resize(final int width, final int height) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.resize(width, height);
		}
	}

	@Override
	public void drawBuffer(final Graphics2D g) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.drawBuffer(g);
		}
	}

	@Override
	public void clear() {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.clear();
		}
	}
}
