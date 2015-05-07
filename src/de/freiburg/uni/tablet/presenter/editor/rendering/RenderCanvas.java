package de.freiburg.uni.tablet.presenter.editor.rendering;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.SwingUtilities;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBuffer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PagePenDispatcher;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.IToolbarItem;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.ToolbarRenderer;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.IToolListener;

public class RenderCanvas extends Canvas implements IPageRenderer {
	private static final long serialVersionUID = 1L;
	
	private boolean _requiresRepaint = true;
	private int _suspendRepaintCounter = 0;
	
	private final Object _repaintMonitor = new Object();
	
	private final Object _paintSync = new Object();
	
	private IPageLayerBuffer _renderBuffer;

	private boolean _running;
	
	private Thread _renderThread;
	
	private int _lastRenderOffsetX = 0;
	private int _lastRenderOffsetY = 0;
	private int _lastRenderWidth = 0;
	private int _lastRenderHeight = 0;
	
	private int _lastWidth = 0;
	private int _lastHeight = 0;
	
	/**
	 * Dispatcher
	 */
	private final PagePenDispatcher _pagePenDispatcher;

	private ToolbarRenderer _toolbarRenderer;

	private Cursor _toolCursor;
	private boolean _hasTemporaryCursor = false;
	
	public RenderCanvas() {
		super();
		setBackground(Color.BLACK);
		
		setPreferredSize(new Dimension(65535, 65535));
		
		_renderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					repaintThread();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				requireRepaint();
			}
		});
		
		_pagePenDispatcher = new PagePenDispatcher();
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(IPen.DEFAULT_PRESSURE);
	}
	
	/**
	 * Start the rendering thread
	 */
	public void start() {
		AwtPenToolkit.addPenListener(RenderCanvas.this, _pagePenDispatcher);
		System.out.println("Pen registered");
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_running = true;
				createBufferStrategy(2);
				_renderThread.start();
				System.out.println("Start render thread");
			}
		});
	}
	
	/**
	 * Stop the rendering thread
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		AwtPenToolkit.removePenListener(RenderCanvas.this, _pagePenDispatcher);
		System.out.println("Pen unregistered");
		
		if (_renderThread.isAlive()) {
			synchronized (_repaintMonitor) {
				_running = false;
				_repaintMonitor.notifyAll();
			}
			try {
				_renderThread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (_renderThread.isAlive()) {
				_renderThread.interrupt();
			}
			try {
				_renderThread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (_renderThread.isAlive()) {
				_renderThread.stop();
			}
		}
	}
	
	/**
	 * Internal thread for repainting.
	 * @throws InterruptedException
	 */
	private void repaintThread() throws InterruptedException {
		while (_running) {
			synchronized (_repaintMonitor) {
				while ((_suspendRepaintCounter > 0 || !_requiresRepaint) && _running) {
					_repaintMonitor.wait();
					// Return to notifying thread
					_repaintMonitor.notifyAll();
				}
				_requiresRepaint = false;
			}
			if (_running) {
				if (!isDisplayable()) {
					Thread.sleep(100);
				}
				// Perform actual repaint
				paint(null);
			}
		}
		System.out.println("Repaint thread exit");
	}
	
	@Override
	public void paint(final Graphics g) {
		synchronized (_paintSync) {
			int width = this.getWidth();
			int height = this.getHeight();
			if ((width != _lastWidth) || (height != _lastHeight)) {
				_lastWidth = width;
				_lastHeight = height;
				final Float desiredRatio = _renderBuffer.getDesiredRatio();
				if (desiredRatio == null) {
					_lastRenderWidth = width;
					_lastRenderHeight = height;
					_lastRenderOffsetX = 0;
					_lastRenderOffsetY = 0;
				} else {
					System.out.println("Resize desired ratio: " + desiredRatio);
					_lastRenderWidth = Math.min((int)(height * desiredRatio), width);
					_lastRenderHeight = Math.min((int)(width / desiredRatio), height);
					_lastRenderOffsetX = (_lastWidth - _lastRenderWidth) / 2;
					_lastRenderOffsetY = (_lastHeight - _lastRenderHeight) / 2;
				}
				_pagePenDispatcher.setDrawSize(new Dimension(_lastRenderWidth, _lastRenderHeight), new Point(_lastRenderOffsetX, _lastRenderOffsetY));
				if (_renderBuffer != null) {
					_renderBuffer.resize(_lastRenderWidth, _lastRenderHeight, _lastRenderOffsetX, _lastRenderOffsetY);
				}
				if (_toolbarRenderer != null) {
					_toolbarRenderer.updateBounds(width, height);
				}
			}
			
			if (this.isVisible()) {
				final BufferStrategy strategy = this.getBufferStrategy();
				final Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
				
				// Clear unused area
				if (_lastRenderOffsetX != 0 || _lastRenderOffsetY != 0 || width - _lastRenderWidth != 0 || height - _lastRenderHeight != 0) {
					graphics.setPaint(Color.BLACK);
					graphics.fillRect(0, 0, width, _lastRenderOffsetY);
					graphics.fillRect(0, 0, _lastRenderOffsetX, height);
					graphics.fillRect(_lastRenderOffsetX + _lastRenderWidth, 0, width, height);
					graphics.fillRect(0, _lastRenderOffsetY + _lastRenderHeight, width, height);
				}
				
				if (_renderBuffer != null) {
					try {
						_renderBuffer.drawBuffer(graphics, this);
					} catch(Exception e) {
						e.printStackTrace();
						requireRepaint();
					}
				}
				
				if (graphics != null) {
					if (_toolbarRenderer != null) {
						_toolbarRenderer.paint(graphics);
					}
					graphics.dispose();
				}
				try {
					strategy.show();
					Toolkit.getDefaultToolkit().sync();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					_requiresRepaint = true;
				}
			}
		}
	}
	
	public void setToolbar(final IToolbarItem[] actions, final int orientation, final int compactSize, final float compactOpacity) {
		_toolbarRenderer = new ToolbarRenderer(this, orientation, compactSize, compactOpacity, getFont());
		_toolbarRenderer.setActions(actions);
		_pagePenDispatcher.setFilter(_toolbarRenderer);
	}
	
	@Override
	public void setDisplayedPageLayerBuffer(final IPageLayerBuffer pageLayer) {
		synchronized (_paintSync) {
			_renderBuffer = pageLayer;
			if (_renderBuffer != null) {
				_renderBuffer.resize(_lastRenderWidth, _lastRenderHeight, _lastRenderOffsetX, _lastRenderOffsetY);
			}
		}
		requireRepaint();
	}
	
	@Override
	public IPageLayerBuffer getPageLayer() {
		return _renderBuffer;
	}
	
	@Override
	public void update(final Graphics g) {
		this.paint(g);
	}
	
	@Override
	public void requireRepaint() {
		synchronized (_repaintMonitor) {
			_requiresRepaint = true;
			if (_suspendRepaintCounter == 0) {
				_repaintMonitor.notifyAll();
			}
		}
	}
	
	@Override
	public void suspendRepaint() {
		synchronized (_repaintMonitor) {
			_suspendRepaintCounter++;
		}
	}
	
	@Override
	public void resumeRepaint() {
		synchronized (_repaintMonitor) {
			_suspendRepaintCounter--;
			if (_suspendRepaintCounter == 0 && _requiresRepaint) {
				_repaintMonitor.notifyAll();
			}
		}
	}
	
	@Override
	public BufferedImage createImageBuffer(final int width, final int height, final int transparency) {
		return getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
	}
	
	@Override
	public boolean isWorking() {
		return isVisible();
	}
	
	@Override
	public ImageObserver getObserver() {
		return this;
	}
	
	@Override
	public Component getContainerComponent() {
		return this;
	}
	
	/**
	 * Locks the pressure (disable pen pressure detection)
	 * @param lockPressure
	 */
	public void setLockPressure(final boolean lockPressure) {
		_pagePenDispatcher.setLockPressure(lockPressure);
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
	public void setNormalToolOnce(final ITool onceNormalTool) {
		final ITool currentTool = getNormalTool();
		onceNormalTool.addToolListener(new IToolListener() {
			@Override
			public void onStart() {
			}
			
			@Override
			public void onFinish(final IRenderable createdRenderable) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setNormalTool(currentTool);
					}
				});
			}
		});
		setNormalTool(onceNormalTool);
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
	public void stopTool() {
		_pagePenDispatcher.stopTool();
	}
	
	@Override
	public void updateTool() {
		_pagePenDispatcher.getNormalTool().updateTool();
		_pagePenDispatcher.getInvertedTool().updateTool();
	}
	
	@Override
	public void setTemporaryCursor(final Cursor cursor) {
		setCursor(cursor);
		_hasTemporaryCursor = true;
	}
	
	@Override
	public void resetTemporaryCursor() {
		setCursor(_toolCursor);
		_hasTemporaryCursor = false;
	}
	
	@Override
	public void setToolCursor(final Cursor cursor) {
		if (!_hasTemporaryCursor) {
			setCursor(cursor);
		}
		_toolCursor = cursor;
	}

	@Override
	public Color getVoidColor() {
		return getBackground();
	}

	@Override
	public void setVoidColor(final Color voidColor) {
		setBackground(voidColor);
	}
}
