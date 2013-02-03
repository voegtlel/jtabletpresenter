package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

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
	
	
	private Graphics2D _graphics = null;
	private Image _imageBuffer = null;
	
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

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

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
						_repaintAddObjects = new LinkedElementList<>();
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
	protected abstract void setRenderingHints(Graphics2D g);
	
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

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
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
			if (_graphics != null) {
				_graphics.dispose();
			}

			final int imgWidth = Math.max(_renderWidth, 1);
			final int imgHeight = Math.max(_renderHeight, 1);
			_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
					Transparency.TRANSLUCENT);
			_graphics = (Graphics2D) _imageBuffer.getGraphics();
			setRenderingHints(_graphics);
			_graphics.setBackground(new Color(0, true));
			_isEmpty = true;
			System.out.println("Resized: " + _renderWidth + "x" + _renderHeight);
		}
		if (requireRepaint > REPAINT_NONE) {
			if (requireRepaint <= REPAINT_CLEAR_RECT) {
				repaintMinX = repaintMinX * _renderFactorX - repaintRadius - 1;
				repaintMinY = repaintMinY * _renderFactorY - repaintRadius - 1;
				repaintMaxX = repaintMaxX * _renderFactorX + repaintRadius + 2;
				repaintMaxY = repaintMaxY * _renderFactorY + repaintRadius + 2;
				/*System.out.println("repaint clip: " + repaintMinX + ", " + repaintMinY + "; " + repaintMaxX + ", " + repaintMaxY);
				_graphics.setClip(null);
				_graphics.setColor(Color.red);
				_graphics.drawRect((int)repaintMinX, (int)repaintMinY, (int)(repaintMaxX - repaintMinX), (int)(repaintMaxY - repaintMinY));*/
				_graphics.setClip((int)repaintMinX, (int)repaintMinY, (int)(repaintMaxX - repaintMinX), (int)(repaintMaxY - repaintMinY));
			} else {
				_graphics.setClip(null);
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
			g.drawImage(_imageBuffer, 0, 0, obs);
		}
	}
	
	/**
	 * Clears the graphics
	 */
	public void clear() {
		if (_graphics != null) {
			_graphics.clearRect(0, 0, _renderWidth, _renderHeight);
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
			_lineRenderer.x1 = x1 * _renderFactorX;
			_lineRenderer.y1 = y1 * _renderFactorY;
			_lineRenderer.x2 = x2 * _renderFactorX;
			_lineRenderer.y2 = y2 * _renderFactorY;
			_graphics.setStroke(pen.getStroke());
			_graphics.setPaint(pen.getColor());
			_graphics.draw(_lineRenderer);
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
			_ellipseRenderer.x = x * _renderFactorX - pen.getThickness() / 2.0f;
			_ellipseRenderer.y = y * _renderFactorY - pen.getThickness() / 2.0f;
			_ellipseRenderer.width = pen.getThickness();
			_ellipseRenderer.height = pen.getThickness();
			_graphics.setPaint(pen.getColor());
			_graphics.fill(_ellipseRenderer);
			_isEmpty = false;
		}
	}

	/**
	 * Draws a path
	 * 
	 * @param pen
	 * @param path
	 */
	public void draw(final IPen pen, final Path2D path) {
		if (_graphics != null) {
			_graphics.setPaint(pen.getColor());
			_graphics.setStroke(pen.getStroke());
			final Shape transformedPath = path
					.createTransformedShape(AffineTransform.getScaleInstance(
							_renderFactorX, _renderFactorY));
			_graphics.draw(transformedPath);
			_isEmpty = false;
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
	public void draw(final BufferedImage image, final float x, final float y, final float width, final float height) {
		if (_graphics != null) {
			final AffineTransform t = AffineTransform.getScaleInstance(_renderFactorX, _renderFactorY);
			t.translate(x, y);
			t.scale(width/(float)image.getWidth(_displayRenderer.getObserver()), height/(float)image.getHeight(_displayRenderer.getObserver()));
			_graphics.drawImage(image, t, _displayRenderer.getObserver());
			_isEmpty = false;
		}
	}
}
