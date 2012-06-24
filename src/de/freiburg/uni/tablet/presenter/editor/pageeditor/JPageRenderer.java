package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class JPageRenderer extends Component implements IPageEditor,
		IDisplayRenderer {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The default page buffer
	 */
	private IPageLayerBuffer _pageLayer = new PageLayerBufferNull();

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

	public JPageRenderer() {
		// setDoubleBuffered(false);
		setIgnoreRepaint(true);

		AwtPenToolkit.addPenListener(this, _pagePenDispatcher);
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);
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
			_pageLayer.drawBuffer((Graphics2D) g);
			System.out.println("Paint");
		}
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
			_pageLayer.resize(_lastRenderDimensions.width,
					_lastRenderDimensions.height);
			// Update pen listener
			_pagePenDispatcher.setDrawSize(_lastRenderDimensions);
		}
	}

	@Override
	public void clear() {
		if (isVisible()) {
			final Graphics2D g = createRenderer();
			_pageLayer.drawBuffer(g);
			updateRenderer(g);
		}
	}

	@Override
	public IPageLayerBuffer getPageLayer() {
		return _pageLayer;
	}

	@Override
	public void setPageLayer(final IPageLayerBuffer pageLayer) {
		_pageLayer = pageLayer;
	}

	@Override
	public void updateRenderer(final Graphics2D g) {
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
		if (_paintSynch != null) {
			synchronized (_paintSynch) {
				repaint(0, 0, 1, 1);
				try {
					_paintSynch.wait(500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
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
