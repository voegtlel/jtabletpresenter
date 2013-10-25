package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.util.LinkedList;

public class PageLayerBufferComposite implements IPageLayerBuffer {
	public static final int PDF_LIBRARY_TYPE_JPOD = 0;
	public static final int PDF_LIBRARY_TYPE_MUPDF = 1;
	
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
	
	public IPageLayerBufferPdf addPdfBuffer(final int libraryType) {
		final IPageLayerBufferPdf result;
		if (libraryType == PDF_LIBRARY_TYPE_JPOD) {
			result = new PageLayerBufferPdf(
					_displayRenderer);
		} else if (libraryType == PDF_LIBRARY_TYPE_MUPDF) {
			result = new PageLayerBufferPdf2(
					_displayRenderer);
		} else {
			throw new IllegalArgumentException("libraryType for pdf layer invalid");
		}
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
	public void resize(final int width, final int height, final int offsetX, final int offsetY) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.resize(width, height, offsetX, offsetY);
		}
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.drawBuffer(g, obs);
		}
	}

	@Override
	public Float getDesiredRatio() {
		Float result = null;
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			final Float tmp = pageLayerBuffer.getDesiredRatio();
			if (tmp != null) {
				result = tmp;
			}
		}
		return result;
	}
}
