package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPen;

public abstract class AbstractPageLayerBuffer implements IPageLayerBuffer {
	// Repaint nothing
	private final static int REPAINT_NONE = 0;
	// Only add a renderable on top
	private final static int REPAINT_ADD_PAINT = 1;
	// Repaint only rect
	private final static int REPAINT_CLEAR_RECT = 2;
	// Repaint whole image
	private final static int REPAINT_ALL = 3;
	// Recreate whole image
	private final static int REPAINT_RESIZE = 4;
	
	
	private Canvas _graphics = null;
	private Bitmap _graphicsBitmap = null;
	private Paint _paint = new Paint();
	
	private Object _repaintSync = new Object();

	private int _newWidth = 1;
	private int _newHeight = 1;
	private int _requireRepaint = REPAINT_ALL;
	
	private LinkedElementList<IRenderable> _repaintAddObjects = null;
	private float _repaintMinX = 1;
	private float _repaintMinY = 1;
	private float _repaintMaxX = 0;
	private float _repaintMaxY = 0;
	private float _repaintRadius = 0;
	
	private boolean _isEmpty = true;

	protected int _renderWidth = 1;
	protected int _renderHeight = 1;

	protected float _renderFactorX = 1;
	protected float _renderFactorY = 1;
	protected final IDisplayRenderer _displayRenderer;
	
	protected RectF _tempRect = new RectF();
	
	private float[] _ptsBuffer = new float[]{};

	public AbstractPageLayerBuffer(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}
	
	/**
	 * Called to mark that a repaint is required
	 */
	public void requireRepaint() {
		synchronized (_repaintSync) {
			if (_requireRepaint < REPAINT_ALL) {
				_requireRepaint = REPAINT_ALL;
				_displayRenderer.requireRepaint();
			}
		}
	}
	
	/**
	 * Requires repainting the given renderable.
	 * 
	 * @param renderable the renderable to (re)draw
	 * @param clear if true, the rectangle below the renderable is cleared and also redrawn,
	 * otherwise only the renderable is added
	 */
	public void requireRepaint(final IRenderable renderable, final boolean clear) {
		synchronized (_repaintSync) {
			if (_requireRepaint <= REPAINT_CLEAR_RECT) {
				_repaintMinX = Math.min(_repaintMinX, renderable.getMinX());
				_repaintMinY = Math.min(_repaintMinY, renderable.getMinY());
				_repaintMaxX = Math.max(_repaintMaxX, renderable.getMaxX());
				_repaintMaxY = Math.max(_repaintMaxY, renderable.getMaxY());
				_repaintRadius = Math.max(_repaintRadius, renderable.getRadius());
				if (_requireRepaint <= REPAINT_ADD_PAINT && !clear) {
					if (_repaintAddObjects == null) {
						_repaintAddObjects = new LinkedElementList<IRenderable>();
					}
					_repaintAddObjects.addLast(renderable);
					_requireRepaint = REPAINT_ADD_PAINT;
				} else {
					_requireRepaint = REPAINT_CLEAR_RECT;
				}
			}
			_displayRenderer.requireRepaint();
		}
	}
	
	/**
	 * Sets the rendering hints for the given graphics object
	 * 
	 * @param g
	 */
	protected abstract void setRenderingHints(Paint g);
	
	/**
	 * Repaints all objects to graphics
	 */
	protected abstract void repaint();
	
	/**
	 * Paints one object to graphics
	 */
	protected abstract void paint(IRenderable renderable);

	@Override
	public void resize(final int width, final int height) {
		synchronized (_repaintSync) {
			_newWidth = width;
			_newHeight = height;
			if (_requireRepaint < REPAINT_RESIZE) {
				_requireRepaint = REPAINT_RESIZE;
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	public void drawBuffer(final Canvas g) {
		int requireRepaint;
		LinkedElementList<IRenderable> repaintAddObjects;
		float repaintMinX;
		float repaintMinY;
		float repaintMaxX;
		float repaintMaxY;
		float repaintRadius;
		synchronized (_repaintSync) {
			_renderWidth = _newWidth;
			_renderHeight = _newHeight;
			_renderFactorX = _renderWidth;
			_renderFactorY = _renderHeight;
			requireRepaint = _requireRepaint;
			_requireRepaint = REPAINT_NONE;
			
			repaintAddObjects = _repaintAddObjects;
			repaintMinX = _repaintMinX;
			repaintMinY = _repaintMinY;
			repaintMaxX = _repaintMaxX;
			repaintMaxY = _repaintMaxY;
			repaintRadius = _repaintRadius;
			_repaintAddObjects = null;
			_repaintMinX = 1;
			_repaintMinY = 1;
			_repaintMaxX = 0;
			_repaintMaxY = 0;
			_repaintRadius = 0;
		}
		if (requireRepaint >= REPAINT_RESIZE) {
			final int imgWidth = Math.max(_renderWidth, 1);
			final int imgHeight = Math.max(_renderHeight, 1);
			if (_graphicsBitmap != null) {
				_graphicsBitmap.recycle();
			}
			_graphicsBitmap = _displayRenderer.createImageBuffer(imgWidth, imgHeight);
			_graphicsBitmap.eraseColor(0x00000000);
			_graphicsBitmap.setHasAlpha(true);
			_graphicsBitmap.setDensity(Bitmap.DENSITY_NONE);
			_graphics = new Canvas(_graphicsBitmap);
			_graphics.setDensity(Bitmap.DENSITY_NONE);
			clear();
			setRenderingHints(_paint);
			_isEmpty = true;
			System.out.println("Resized: " + _renderWidth + "x" + _renderHeight);
		}
		if (requireRepaint > REPAINT_NONE) {
			if (requireRepaint <= REPAINT_CLEAR_RECT) {
				repaintMinX = repaintMinX * _renderFactorX - repaintRadius - 1;
				repaintMinY = repaintMinY * _renderFactorY - repaintRadius - 1;
				repaintMaxX = repaintMaxX * _renderFactorX + repaintRadius + 1;
				repaintMaxY = repaintMaxY * _renderFactorY + repaintRadius + 1;
				//System.out.println("repaint clip: " + repaintMinX + ", " + repaintMinY + "; " + repaintMaxX + ", " + repaintMaxY);
				_graphics.clipRect(repaintMinX, repaintMinY, repaintMaxX, repaintMaxY, Region.Op.REPLACE);
			} else {
				_graphics.clipRect(0, 0, _renderWidth, _renderHeight, Region.Op.REPLACE);
			}
			if (requireRepaint >= REPAINT_CLEAR_RECT) {
				if (_graphics != null) {
					repaint();
				}
			} else if (requireRepaint == REPAINT_ADD_PAINT) {
				_isEmpty = false;
				for (LinkedElement<IRenderable> element = repaintAddObjects.getFirst(); element != null; element = element.getNext()) {
					paint(element.getData());
				}
			}
		}
		if (!_isEmpty) {
			_graphicsBitmap.prepareToDraw();
			
			Rect srcDst = new Rect(0, 0, _renderWidth, _renderHeight);
			g.drawBitmap(_graphicsBitmap, srcDst, srcDst, null);
		}
	}
	
	/**
	 * Clears the graphics
	 */
	public void clear() {
		if (_graphics != null) {
			_graphics.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
			_isEmpty = true;
		}
	}

	/**
	 * Draws a line
	 * 
	 * @param pen
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void draw(final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		if (_graphics != null) {
			_paint.setStrokeCap(pen.getStrokeCap());
			_paint.setStrokeJoin(pen.getStrokeJoin());
			_paint.setStrokeMiter(pen.getStrokeMiter());
			_paint.setStrokeWidth(pen.getStrokeWidth());
			_paint.setColor(pen.getColor());
			_paint.setStyle(Paint.Style.STROKE);
			_graphics.drawLine(x1 * _renderFactorX, y1 * _renderFactorY, x2 * _renderFactorX, y2 * _renderFactorY, _paint);
			_isEmpty = false;
		}
	}

	/**
	 * Draws a dot
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	public void draw(final IPen pen, final float x,
			final float y) {
		if (_graphics != null) {
			_paint.setColor(pen.getColor());
			_paint.setStyle(Paint.Style.FILL);
			_tempRect.left = x * _renderFactorX - pen.getThickness() / 2.0f;
			_tempRect.top = y * _renderFactorY - pen.getThickness() / 2.0f;
			_tempRect.right = _tempRect.left + pen.getThickness();
			_tempRect.bottom = _tempRect.top + pen.getThickness();
			_graphics.drawOval(_tempRect, _paint);
			_isEmpty = false;
		}
	}
	
	/**
	 * Draws a dot
	 * 
	 * @param pen
	 * @param x
	 * @param y
	 */
	public void drawEraser(final IPen pen, final float x, final float y) {
		if (_graphics != null) {
			_paint.setColor(pen.getColor());
			_paint.setStyle(Paint.Style.FILL);
			_tempRect.left = x * _renderFactorX - pen.getThickness() / 2.0f;
			_tempRect.top = y * _renderFactorY - pen.getThickness() / 2.0f;
			_tempRect.right = _tempRect.left + pen.getThickness();
			_tempRect.bottom = _tempRect.top + pen.getThickness();
			_graphics.drawOval(_tempRect, _paint);
			_paint.setColor(0xffffffff - (pen.getColor() & 0x00ffffff));
			_paint.setStyle(Paint.Style.STROKE);
			_graphics.drawOval(_tempRect, _paint);
			_isEmpty = false;
		}
	}
	
	/**
	 * Draws a path
	 * 
	 * @param pen
	 * @param path
	 */
	public void draw(final IPen pen, final LinkedElementList<DataPoint> path) {
		if (_graphics != null) {
			_paint.setStrokeCap(pen.getStrokeCap());
			_paint.setStrokeJoin(pen.getStrokeJoin());
			_paint.setStrokeMiter(pen.getStrokeMiter());
			_paint.setStrokeWidth(pen.getStrokeWidth());
			_paint.setColor(pen.getColor());
			_paint.setStyle(Paint.Style.STROKE);
			LinkedElement<DataPoint> point = path.getFirst();
			int count = path.getCount();
			if (_ptsBuffer.length < count << 2) {
				_ptsBuffer = new float[count << 2];
			}
			if (point != null) {
				int i = 0;
				float lastX = point.getData().getX() * _renderFactorX;
				float lastY = point.getData().getY() * _renderFactorY;
				point = point.getNext();
				while (point != null) {
					_ptsBuffer[i++] = lastX;
					_ptsBuffer[i++] = lastY;
					_ptsBuffer[i++] = lastX = point.getData().getX() * _renderFactorX;
					_ptsBuffer[i++] = lastY = point.getData().getY() * _renderFactorY;
					point = point.getNext();
				}
				
				_graphics.drawLines(_ptsBuffer, 0, i, _paint);
				_isEmpty = false;
			}
		}
	}
	
	/**
	 * Draws an image
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param obs (optional)
	 */
	public void draw(final Bitmap image, final float x, final float y, final float width, final float height) {
		if (_graphics != null) {
			final Matrix t = new Matrix();
			t.postScale(_renderFactorX, _renderFactorY);
			t.postTranslate(x, y);
			t.postScale(width/(float)image.getWidth(), height/(float)image.getHeight());
			_graphics.drawBitmap(image, t, _paint);
			_isEmpty = false;
		}
	}
}
