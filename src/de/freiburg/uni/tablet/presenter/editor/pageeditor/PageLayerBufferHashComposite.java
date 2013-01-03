package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.LinkedList;

public class PageLayerBufferHashComposite<T> implements IPageLayerBuffer {
	private final IDisplayRenderer _displayRenderer;

	private final LinkedList<IPageLayerBuffer> _pageLayerBuffers = new LinkedList<IPageLayerBuffer>();
	private final HashMap<T, PageLayerBufferFront> _pageLayerBuffersFront = new HashMap<T, PageLayerBufferFront>();
	private final HashMap<T, PageLayerBufferBack> _pageLayerBuffersBack = new HashMap<T, PageLayerBufferBack>();

	public PageLayerBufferHashComposite(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}

	/**
	 * Returns a valid buffer for the given id. If the buffer does not exist, it
	 * will be created.
	 * 
	 * @param id
	 * @return
	 */
	public PageLayerBufferFront getFrontBuffer(final T id) {
		PageLayerBufferFront result = _pageLayerBuffersFront.get(id);
		if (result == null) {
			result = new PageLayerBufferFront(_displayRenderer);
			_pageLayerBuffers.add(result);
			_pageLayerBuffersFront.put(id, result);
		}
		return result;
	}

	/**
	 * Returns a valid buffer for the given id. If the buffer does not exist, it
	 * will be created.
	 * 
	 * @param id
	 * @return
	 */
	public PageLayerBufferBack getBackBuffer(final T id) {
		PageLayerBufferBack result = _pageLayerBuffersBack.get(id);
		if (result == null) {
			result = new PageLayerBufferBack(_displayRenderer);
			_pageLayerBuffers.add(result);
			_pageLayerBuffersBack.put(id, result);
		}
		return result;
	}

	/**
	 * Removes a buffer by its id
	 * 
	 * @param id
	 */
	public void removeBufferFront(final T id) {
		final PageLayerBufferFront buffer = _pageLayerBuffersFront.get(id);
		_pageLayerBuffers.remove(buffer);
		_pageLayerBuffersFront.remove(id);
	}

	/**
	 * Removes a buffer by its id
	 * 
	 * @param id
	 */
	public void removeBufferBack(final T id) {
		final PageLayerBufferBack buffer = _pageLayerBuffersBack.get(id);
		_pageLayerBuffers.remove(buffer);
		_pageLayerBuffersBack.remove(id);
	}

	@Override
	public void resize(final int width, final int height) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.resize(width, height);
		}
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.drawBuffer(g, obs);
		}
	}

	@Override
	public void clear() {
		for (final IPageLayerBuffer pageLayerBuffer : _pageLayerBuffers) {
			pageLayerBuffer.clear();
		}
	}
}
