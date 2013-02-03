package de.freiburg.uni.tablet.presenter.editor.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBuffer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PagePenDispatcher;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class RenderCanvas extends SurfaceView implements IPageRenderer {
	private boolean _requiresRepaint = true;
	private int _suspendRepaintCounter = 0;
	
	private final Object _repaintMonitor = new Object();
	
	private final Object _paintSync = new Object();
	
	private IPageLayerBuffer _renderBuffer;

	private boolean _running;
	
	private Thread _renderThread;
	
	private int _lastRenderWidth = 0;
	private int _lastRenderHeight = 0;
	
	private SurfaceHolder _surfaceHolder;
	
	/**
	 * Dispatcher
	 */
	private PagePenDispatcher _pagePenDispatcher;
	
	public RenderCanvas(final Context context) {
		super(context);
		initialize(context);
	}
	
	public RenderCanvas(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public RenderCanvas(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}
	
	
	private final void initialize(Context context) {
		System.out.println("Init render canvas");
		
		_surfaceHolder = getHolder();
		_surfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(final SurfaceHolder holder) {
				stop();
			}
			
			@Override
			public void surfaceCreated(final SurfaceHolder holder) {
				start();
			}
			
			@Override
			public void surfaceChanged(final SurfaceHolder holder, final int format, final int width,
					final int height) {
				requireRepaint();
			}
		});
		
		_pagePenDispatcher = new PagePenDispatcher();
		_pagePenDispatcher.setDrawSize(this.getWidth(), this.getHeight());
		System.out.println("Init render canvas done");
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		requireRepaint();
	}
	
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		//super.onTouchEvent(event);
		_pagePenDispatcher.onTouchEvent(event);
		return true;
	}
	
	/**
	 * Start the rendering thread
	 */
	private void start() {
		stop();
		_running = true;
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
		_renderThread.start();
		System.out.println("Start render thread");
	}
	
	/**
	 * Stop the rendering thread
	 */
	private void stop() {
		if (_renderThread != null && _renderThread.isAlive()) {
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
					_repaintMonitor.wait();
					// Return to notifying thread
					_repaintMonitor.notifyAll();
				}
				_requiresRepaint = false;
			}
			if (_running) {
				// Perform actual repaint
				paint();
			}
		}
		System.out.println("Repaint thread exit");
	}
	
	@Override
	protected void onDraw(final Canvas canvas) {
		synchronized (_paintSync) {
			if (_renderBuffer != null) {
				try {
					_renderBuffer.drawBuffer(canvas);
				} catch(Exception e) {
					e.printStackTrace();
					requireRepaint();
				}
			}
		}
	}
	
	private void paint() {
		synchronized (_paintSync) {
			int width = this.getWidth();
			int height = this.getHeight();
			if ((width != _lastRenderWidth) || (height != _lastRenderHeight)) {
				_lastRenderWidth = width;
				_lastRenderHeight = height;
				_pagePenDispatcher.setDrawSize(width, height);
				if (_renderBuffer != null) {
					_renderBuffer.resize(width, height);
				}
			}
			if (_surfaceHolder.getSurface().isValid()) {
				Canvas lockCanvas = null;
				try {
					lockCanvas = _surfaceHolder.lockCanvas();
					synchronized (_surfaceHolder) {
						onDraw(lockCanvas);
					}
				} finally {
					if (lockCanvas != null) {
						_surfaceHolder.unlockCanvasAndPost(lockCanvas);
					}
				}
			} else {
				requireRepaint();
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
	public Bitmap createImageBuffer(int width, int height) {
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}
	
	@Override
	public boolean isWorking() {
		return _surfaceHolder.getSurface().isValid();
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
