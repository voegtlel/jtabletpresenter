package de.freiburg.uni.tablet.presenter.editor.rendering;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.awt.image.ImageObserver;

import javax.swing.SwingUtilities;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBuffer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PagePenDispatcher;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class RenderCanvas extends Canvas implements IPageRenderer {
	private static final long serialVersionUID = 1L;
	
	private boolean _requiresRepaint = true;
	private int _suspendRepaintCounter = 0;
	
	private final Object _repaintMonitor = new Object();
	
	private final Object _paintSync = new Object();
	
	private IPageLayerBuffer _renderBuffer;

	private boolean _running;
	
	private Thread _renderThread;
	
	private int _lastRenderWidth = 0;
	private int _lastRenderHeight = 0;
	
	/**
	 * Dispatcher
	 */
	private final PagePenDispatcher _pagePenDispatcher;
	
	public RenderCanvas() {
		super();
		setIgnoreRepaint(true);
		
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
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				requireRepaint();
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				stop();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
		});
		
		_pagePenDispatcher = new PagePenDispatcher();
		AwtPenToolkit.addPenListener(this, _pagePenDispatcher);
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);
		start();
	}
	
	/**
	 * Start the rendering thread
	 */
	protected void start() {
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
	protected void stop() {
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
	 * 
	 * @throws InterruptedException
	 */
	private void repaintThread() throws InterruptedException {
		while (_running) {
			synchronized (_repaintMonitor) {
				while ((_suspendRepaintCounter > 0 || !_requiresRepaint) && _running) {
					System.out.println("Repaint wait");
					_repaintMonitor.wait();
					System.out.println("Repaint notify");
					// Return to notifying thread
					_repaintMonitor.notifyAll();
				}
				_requiresRepaint = false;
			}
			System.out.println("Repaint perform");
			if (_running) {
				System.out.println("Repaint draw");
				// Perform actual repaint
				paint(null);
			}
		}
		System.out.println("Repaint Exit");
	}
	
	@Override
	public void paint(final Graphics g) {
		synchronized (_paintSync) {
			int width = this.getWidth();
			int height = this.getHeight();
			if ((width != _lastRenderWidth) || (height != _lastRenderHeight)) {
				_lastRenderWidth = width;
				_lastRenderHeight = height;
				_pagePenDispatcher.setDrawSize(new Dimension(width, height));
				if (_renderBuffer != null) {
					_renderBuffer.resize(width, height);
				}
			}
			
			if (this.isVisible()) {
				final BufferStrategy strategy = this.getBufferStrategy();
				final Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
				
				if (_renderBuffer != null) {
					_renderBuffer.drawBuffer(graphics, this);
				}
				
				if (graphics != null) {
					graphics.dispose();
				}
				strategy.show();
				Toolkit.getDefaultToolkit().sync();
			}
		}
	}
	
	@Override
	public void setDisplayedPageLayerBuffer(final IPageLayerBuffer pageLayer) {
		synchronized (_paintSync) {
			_renderBuffer = pageLayer;
			if (_renderBuffer != null) {
				_renderBuffer.resize(_lastRenderWidth, _lastRenderHeight);
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
	public Image createImageBuffer(int width, int height, int transparency) {
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
	public void stopTool() {
		_pagePenDispatcher.stopTool();
	}
}
