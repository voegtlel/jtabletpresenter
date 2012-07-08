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

import de.freiburg.uni.tablet.presenter.page.IPen;

public abstract class AbstractPageLayerBuffer implements IPageLayerBuffer {
	protected Graphics2D _graphics = null;
	protected Image _imageBuffer = null;

	private boolean _isEmpty = true;

	private int _renderWidth = 1;
	private int _renderHeight = 1;

	private float _renderFactorX = 1;
	private float _renderFactorY = 1;
	protected final IDisplayRenderer _displayRenderer;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	public AbstractPageLayerBuffer(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
		if (_displayRenderer.isWorking()) {
			resize(_displayRenderer.getWidth(), _displayRenderer.getHeight());
		}
	}

	/**
	 * Sets the rendering hints for the given graphics object
	 * 
	 * @param g
	 */
	protected abstract void setRenderingHints(Graphics2D g);

	@Override
	public void resize(final int width, final int height) {
		_renderWidth = width;
		_renderHeight = height;
		_renderFactorX = width;
		_renderFactorY = height;

		if (_graphics != null) {
			_graphics.dispose();
		}

		final int imgWidth = Math.max(width, 1);
		final int imgHeight = Math.max(height, 1);
		_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
				Transparency.TRANSLUCENT);
		_graphics = (Graphics2D) _imageBuffer.getGraphics();
		setRenderingHints(_graphics);
		_graphics.setBackground(new Color(0, true));
		_isEmpty = true;
	}

	@Override
	public void drawBuffer(final Graphics2D g) {
		if (!_isEmpty) {
			g.drawImage(_imageBuffer, 0, 0, null);
		}
	}

	/**
	 * Draws a line
	 * 
	 * @param g
	 *            destination graphics
	 * @param pen
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void draw(final Graphics2D g, final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		_lineRenderer.x1 = x1 * _renderFactorX;
		_lineRenderer.y1 = y1 * _renderFactorY;
		_lineRenderer.x2 = x2 * _renderFactorX;
		_lineRenderer.y2 = y2 * _renderFactorY;
		g.setStroke(pen.getStroke());
		g.setPaint(pen.getColor());
		g.draw(_lineRenderer);
		_isEmpty = false;
	}

	/**
	 * Draws a dot
	 * 
	 * @param g
	 *            destination graphics
	 * @param pen
	 * @param x
	 * @param y
	 */
	protected void draw(final Graphics2D g, final IPen pen, final float x,
			final float y) {
		_ellipseRenderer.x = x * _renderFactorX - pen.getThickness() / 2.0f;
		_ellipseRenderer.y = y * _renderFactorY - pen.getThickness() / 2.0f;
		_ellipseRenderer.width = pen.getThickness();
		_ellipseRenderer.height = pen.getThickness();
		g.setPaint(pen.getColor());
		g.fill(_ellipseRenderer);
		_isEmpty = false;
	}

	/**
	 * Draws a path
	 * 
	 * @param g
	 *            destination graphics
	 * @param pen
	 * @param path
	 */
	protected void draw(final Graphics2D g, final IPen pen, final Path2D path) {
		g.setPaint(pen.getColor());
		g.setStroke(pen.getStroke());
		final Shape transformedPath = path
				.createTransformedShape(AffineTransform.getScaleInstance(
						_renderFactorX, _renderFactorY));
		g.draw(transformedPath);
		_isEmpty = false;
	}

	@Override
	public void clear() {
		if (_graphics != null) {
			_isEmpty = true;
			_graphics.clearRect(0, 0, _renderWidth, _renderHeight);
		}
	}
}
