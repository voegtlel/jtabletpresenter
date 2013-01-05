package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;

import javax.swing.SwingUtilities;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class JPageRendererBufferStrategy extends Canvas implements
		IPageRenderer {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The default page buffer
	 */
	private IPageLayerBuffer _displayedPageLayerBuffer = new PageLayerBufferNull();

	/**
	 * Dimensions, when the component was last drawn
	 */
	private Dimension _lastRenderDimensions = new Dimension();

	/**
	 * Dispatcher
	 */
	private final PagePenDispatcher _pagePenDispatcher = new PagePenDispatcher();

	private long _nextFrameTime = 0;

	/**
	 * Buffer strategy
	 */
	private BufferStrategy _bufferStrategy;

	public JPageRendererBufferStrategy() {
		AwtPenToolkit.addPenListener(this, _pagePenDispatcher);
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPageRendererBufferStrategy.this.initialize();
			}
		});
	}

	@Override
	public void paint(final Graphics g) {
		clear();
	}

	@Override
	public void update(final Graphics g) {
		this.paint(g);
	}

	private void initialize() {
		createBufferStrategy(2);
		_bufferStrategy = getBufferStrategy();
		System.out.println("Inited: " + _bufferStrategy);
	}

	private void render(final boolean force) {
		boolean dimensionsUpdated = false;
		if (!_lastRenderDimensions.equals(this.getSize())) {
			_lastRenderDimensions = this.getSize();
			initializeGraphics();
			dimensionsUpdated = true;
		}
		final long time = System.nanoTime() / (1000 * 1000);
		if (force || dimensionsUpdated || _nextFrameTime < time) {
			_nextFrameTime = time + _pagePenDispatcher.getFrameReduction();
			if (_bufferStrategy != null) {
				final Graphics2D g = (Graphics2D) _bufferStrategy
						.getDrawGraphics();
				_displayedPageLayerBuffer.drawBuffer(g, this);
				g.dispose();
				_bufferStrategy.show();
			}
			getToolkit().sync();
		}
	}

	@Override
	public void updateRenderer(final Graphics2D dummy) {
		render(false);
	}

	@Override
	public Image createImageBuffer(final int width, final int height,
			final int transparency) {
		return getGraphicsConfiguration().createCompatibleImage(width, height,
				transparency);
	}

	@Override
	public Graphics2D createRenderer() {
		return null;
	}
	
	@Override
	public ImageObserver getObserver() {
		return null;
	}

	/**
	 * (re)initialized the buffers and drawing destinations
	 */
	private void initializeGraphics() {
		System.out.println("initGraphics");
		if (_lastRenderDimensions.width > 0 && _lastRenderDimensions.height > 0
				&& this.isVisible()) {

			_displayedPageLayerBuffer.resize(_lastRenderDimensions.width,
					_lastRenderDimensions.height);
			// Update pen listener
			_pagePenDispatcher.setDrawSize(_lastRenderDimensions);
		}
	}

	@Override
	public void clear() {
		if (isVisible()) {
			render(true);
		}
	}

	@Override
	public void clear(final IRenderable renderable) {
		if (isVisible()) {
			render(true);
		}
	}

	@Override
	public IPageLayerBuffer getPageLayer() {
		return _displayedPageLayerBuffer;
	}

	@Override
	public void setDisplayedPageLayerBuffer(
			final IPageLayerBuffer pageLayerBuffer) {
		_displayedPageLayerBuffer = pageLayerBuffer;
	}

	@Override
	public boolean isWorking() {
		return isVisible();
	}

	@Override
	public ITool getNormalTool() {
		return _pagePenDispatcher.getNormalTool();
	}

	@Override
	public void setNormalTool(final ITool normalTool) {
		_pagePenDispatcher.setNormalTool(normalTool);
	}

	@Override
	public ITool getInvertedTool() {
		return _pagePenDispatcher.getInvertedTool();
	}

	@Override
	public void setInvertedTool(final ITool invertedTool) {
		_pagePenDispatcher.setInvertedTool(invertedTool);
	}

	@Override
	public Component getContainerComponent() {
		return this;
	}
}
