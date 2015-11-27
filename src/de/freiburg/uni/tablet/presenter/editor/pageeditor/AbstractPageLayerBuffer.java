package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;
import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.pdf.cos.COSName;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformDevice;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public abstract class AbstractPageLayerBuffer implements IPageLayerBuffer, IPageBackRenderer {
	// Repaint nothing (only reblit to graphics)
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
	
	private Float _desiredRatio;

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

	protected final IDisplayRenderer _displayRenderer;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();
	
	private RenderMetric _currentRenderMetric = new RenderMetric();
	
	public AbstractPageLayerBuffer(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}
	
	/**
	 * Called to mark that a repaint is required
	 */
	@Override
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
	@Override
	public void requireRepaint(final IRenderable renderable, final boolean clear) {
		synchronized (_repaintSync) {
			if (_requireRepaint <= REPAINT_CLEAR_RECT) {
				_repaintMinX = Math.min(_repaintMinX, renderable.getMinX());
				_repaintMinY = Math.min(_repaintMinY, renderable.getMinY());
				_repaintMaxX = Math.max(_repaintMaxX, renderable.getMaxX());
				_repaintMaxY = Math.max(_repaintMaxY, renderable.getMaxY());
				_repaintRadius = Math.max(_repaintRadius, renderable.getRadius(_currentRenderMetric));
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
	public void resize(final RenderMetric renderMetric) {
		synchronized (_repaintSync) {
			if ((_newWidth != renderMetric.surfaceWidth || _newHeight != renderMetric.surfaceHeight) && _requireRepaint < REPAINT_RESIZE) {
				_requireRepaint = REPAINT_RESIZE;
			} else if (_requireRepaint < REPAINT_ALL) {
				_requireRepaint = REPAINT_ALL;
			}
			_newWidth = renderMetric.surfaceWidth;
			_newHeight = renderMetric.surfaceHeight;
		}
	}

	@Override
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
		int requireRepaint;
		LinkedElementList<IRenderable> repaintAddObjects;
		float repaintMinX;
		float repaintMinY;
		float repaintMaxX;
		float repaintMaxY;
		float repaintRadius;
		synchronized (_repaintSync) {
			_currentRenderMetric.copyFrom(renderMetric);
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

			final int imgWidth = Math.max(_currentRenderMetric.surfaceWidth, 1);
			final int imgHeight = Math.max(_currentRenderMetric.surfaceHeight, 1);
			_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
					Transparency.TRANSLUCENT);
			_graphics = (Graphics2D) _imageBuffer.getGraphics();
			setRenderingHints(_graphics);
			_graphics.setBackground(new Color(0, true));
			_isEmpty = true;
			System.out.println("Resized: " + _currentRenderMetric.surfaceWidth + "x" + _currentRenderMetric.surfaceHeight);
		}
		if (requireRepaint > REPAINT_NONE) {
			if (requireRepaint <= REPAINT_CLEAR_RECT) {
				repaintMinX = repaintMinX * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX - repaintRadius - 1;
				repaintMinY = repaintMinY * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY - repaintRadius - 1;
				repaintMaxX = repaintMaxX * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX + repaintRadius + 2;
				repaintMaxY = repaintMaxY * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY + repaintRadius + 2;
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
			g.drawImage(_imageBuffer, _currentRenderMetric.surfaceDrawOffsetX, _currentRenderMetric.surfaceDrawOffsetY, null);
		}
	}
	
	/**
	 * Clears the graphics
	 */
	public void clear() {
		if (_graphics != null) {
			_graphics.clearRect(0, 0, _currentRenderMetric.surfaceWidth, _currentRenderMetric.surfaceHeight);
			_isEmpty = true;
		}
	}

	@Override
	public void draw(final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		if (_graphics != null) {
			_lineRenderer.x1 = x1 * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX;
			_lineRenderer.y1 = y1 * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY;
			_lineRenderer.x2 = x2 * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX;
			_lineRenderer.y2 = y2 * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY;
			if (pen == null) {
				_graphics.setStroke(new SolidPen().getStroke(_currentRenderMetric, SolidPen.DEFAULT_PRESSURE * _currentRenderMetric.surfaceScale));
				_graphics.setPaint(Color.black);
			} else {
				_graphics.setStroke(pen.getStroke(_currentRenderMetric, SolidPen.DEFAULT_PRESSURE * _currentRenderMetric.surfaceScale));
				_graphics.setPaint(pen.getColor());
			}
			_graphics.draw(_lineRenderer);
			_isEmpty = false;
		}
	}
	
	private IPen _pathPen;
	
	@Override
	public void beginPath(final IPen pen) {
		_pathPen = pen;
		if (_graphics != null) {
			_graphics.setPaint(pen.getColor());
		}
	}
	
	@Override
	public void endPath() {
		_pathPen = null;
	}
	
	@Override
	public void drawPath(final float x1, final float y1, final float x2, final float y2,
			final float pressure) {
		if (_graphics != null) {
			_lineRenderer.x1 = x1 * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX;
			_lineRenderer.y1 = y1 * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY;
			_lineRenderer.x2 = x2 * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX;
			_lineRenderer.y2 = y2 * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY;
			_graphics.setStroke(_pathPen.getStroke(_currentRenderMetric, pressure * _currentRenderMetric.surfaceScale));
			_graphics.draw(_lineRenderer);
			_isEmpty = false;
		}
	}
	
	/**
	 * Draws a debug rectangle
	 */
	@Override
	public void drawDebugRect(final float x1,
			final float y1, final float x2, final float y2) {
		if (_graphics != null) {
			_graphics.setPaint(Color.black);
			_graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			float xMin = Math.min(x1, x2);
			float yMin = Math.min(y1, y2);
			float w = Math.abs(x2 - x1);
			float h = Math.abs(y2 - y1);
			_graphics.draw(new Rectangle2D.Float(xMin * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX,
					yMin * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY,
					w * _currentRenderMetric.innerFactorX,
					h * _currentRenderMetric.innerFactorY));
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
	@Override
	public void draw(final IPen pen, final float x,
			final float y) {
		if (_graphics != null) {
			float thickness = pen.getThickness(_currentRenderMetric, IPen.DEFAULT_PRESSURE);
			_ellipseRenderer.x = x * _currentRenderMetric.innerFactorX + _currentRenderMetric.innerOffsetX - thickness / 2.0f * _currentRenderMetric.surfaceScale;
			_ellipseRenderer.y = y * _currentRenderMetric.innerFactorY + _currentRenderMetric.innerOffsetY - thickness / 2.0f * _currentRenderMetric.surfaceScale;
			_ellipseRenderer.width = thickness;
			_ellipseRenderer.height = thickness;
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
	@Override
	public void draw(final IPen pen, final Path2D path) {
		if (_graphics != null) {
			_graphics.setPaint(pen.getColor());
			_graphics.setStroke(pen.getStroke(_currentRenderMetric));
			AffineTransform t = AffineTransform.getTranslateInstance(_currentRenderMetric.innerOffsetX, _currentRenderMetric.innerOffsetY);
			t.scale(_currentRenderMetric.innerFactorX, _currentRenderMetric.innerFactorY);
			final Shape transformedPath = path
					.createTransformedShape(t);
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
	@Override
	public void draw(final BufferedImage image, final float x, final float y, final float width, final float height) {
		if (_graphics != null) {
			AffineTransform t = AffineTransform.getTranslateInstance(_currentRenderMetric.innerOffsetX, _currentRenderMetric.innerOffsetY);
			t.scale(_currentRenderMetric.innerFactorX, _currentRenderMetric.innerFactorY);
			t.translate(x, y);
			t.scale(width/image.getWidth(null), height/image.getHeight(null));
			_graphics.drawImage(image, t, null);
			_isEmpty = false;
		}
	}
	
	@Override
	public void draw(final float x, final float y, final String[] textLines, final TextFont font) {
		if (_graphics != null) {
			final CwtAwtGraphicsContext ctx = new CwtAwtGraphicsContext(_graphics);
			final CSPlatformRenderer dummyRenderer = new CSPlatformRenderer(null, ctx);
			final CSPlatformDevice csPlatformDevice = new CSPlatformDevice(ctx);
			csPlatformDevice.open(dummyRenderer);
			csPlatformDevice.saveState();
			AffineTransform t = AffineTransform.getTranslateInstance(_currentRenderMetric.innerOffsetX, _currentRenderMetric.innerOffsetY);
			t.scale(_currentRenderMetric.innerFactorX, _currentRenderMetric.innerFactorY);
			t.translate(x, y);
			t.scale(1, -1);
			ctx.setTransform(t);
			csPlatformDevice.textBegin();
			csPlatformDevice.textSetFont(COSName.constant("TextFont"), font.getPDFont(), font.getSize());
			for (int i = 0; i < textLines.length; i++) {
				csPlatformDevice.textShow(textLines[i]);
				if (i < textLines.length - 1) {
					//csPlatformDevice.textLineNew();
					csPlatformDevice.textLineMove(0, -font.getLineHeight());
				}
			}
			csPlatformDevice.textEnd();
			csPlatformDevice.close();
			csPlatformDevice.restoreState();
			_isEmpty = false;
		}
	}
	
	@Override
	public void setOffset(final float x, final float y) {
		_graphics.setTransform(AffineTransform.getTranslateInstance(-x * _currentRenderMetric.innerFactorX, -y * _currentRenderMetric.innerFactorY));
	}
	
	public void setDesiredRatio(final Float desiredRatio) {
		_desiredRatio = desiredRatio;
	}
	
	@Override
	public Float getDesiredRatio() {
		return _desiredRatio;
	}
}
