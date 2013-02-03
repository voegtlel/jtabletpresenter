package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.util.LinkedList;

import android.graphics.Canvas;

public class PageLayerBufferComposite implements IPageLayerBuffer {
	private final IDisplayRenderer _displayRenderer;

	private final LinkedList<IPageLayerBuffer> _pageLayerBuffers = new LinkedList<IPageLayerBuffer>();

	public PageLayerBufferComposite(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}

	public PageLayerBufferFront addFrontBuffer() {
		final PageLayerBufferFront result = new PageLayerBufferFront(
				_displayRenderer);
		_pageLayerBuffers.add(result);
		return result;
	}

	public PageLayerBufferColor addColorBuffer() {
		final PageLayerBufferColor result = new PageLayerBufferColor();
		_pageLayerBuffers.add(result);
		return result;
	}

	public PageLayerBufferBack addBackBuffer() {
		final PageLayerBufferBack result = new PageLayerBufferBack(
				_displayRenderer);
		_pageLayerBuffers.add(result);
		return result;
	}
	
	public PageLayerBufferPdf addPdfBuffer() {
		final PageLayerBufferPdf result = new PageLayerBufferPdf(
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
	public void drawBuffer(final Canvas g) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.drawBuffer(g);
		}
	}
}
