package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class JPageRendererDirect extends Component implements IPageRenderer {
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

	/**
	 * Object for locking the paint-Method.
	 */
	private final Object _paintSynch = new Object();

	public JPageRendererDirect() {
		// setDoubleBuffered(false);
		setIgnoreRepaint(true);

		AwtPenToolkit.addPenListener(this, _pagePenDispatcher);
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);
	}

	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	@Override
	public void paint(final Graphics g) {
		final Rectangle clipBounds = g.getClipBounds();
		if (clipBounds.x == 0 && clipBounds.y == 0 && clipBounds.width == 1
				&& clipBounds.height == 1) {
			// Optimize this!
			g.setColor(getBackground());
			g.fillRect(0, 0, 1, 1);
		} else {
			if (!_lastRenderDimensions.equals(this.getSize())) {
				_lastRenderDimensions = this.getSize();
				initializeGraphics();
			}
			_displayedPageLayerBuffer.drawBuffer((Graphics2D) g, this);
		}
		getToolkit().sync();
		if (_paintSynch != null) {
			synchronized (_paintSynch) {
				_paintSynch.notifyAll();
			}
		}
	}

	/**
	 * (re)initialized the buffers and drawing destinations
	 */
	private void initializeGraphics() {
		// System.out.println("initGraphics");
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
			final Graphics2D g = createRenderer();
			_displayedPageLayerBuffer.drawBuffer(g, this);
			updateRenderer(g);
		}
	}

	@Override
	public void clear(final IRenderable renderable) {
		if (isVisible()) {
			final Graphics2D g = createRenderer();
			final int radius = (int) Math.ceil(renderable.getRadius());
			final int x = (int) (renderable.getMinX() * _lastRenderDimensions.width)
					- radius;
			final int y = (int) (renderable.getMinY() * _lastRenderDimensions.height)
					- radius;
			final int w = (int) Math.ceil(renderable.getMaxX()
					* _lastRenderDimensions.width)
					+ 1 + x + radius;
			final int h = (int) Math.ceil(renderable.getMaxY()
					* _lastRenderDimensions.height)
					+ 1 + y + radius;
			g.clipRect(x, y, w, h);
			_displayedPageLayerBuffer.drawBuffer(g, this);
			updateRenderer(g);
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
	public void updateRenderer(final Graphics2D g) {
		// getToolkit().sync();
		g.dispose();
		getToolkit().sync();
		if (_paintSynch != null) {
			synchronized (_paintSynch) {
				repaint(0, 0, 1, 1);
				if (!EventQueue.isDispatchThread()) {
					try {
						_paintSynch.wait(500);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public Image createImageBuffer(final int width, final int height,
			final int transparency) {
		return getGraphicsConfiguration().createCompatibleImage(width, height,
				transparency);
	}

	@Override
	public Graphics2D createRenderer() {
		return (Graphics2D) this.getGraphics();
	}
	
	@Override
	public ImageObserver getObserver() {
		return this;
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
	
	@Override
	public void stopTool() {
		_pagePenDispatcher.stopTool();
	}
}
