package de.freiburg.uni.tablet.presenter.editor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.IToolContainer;

public class JPageRenderer extends Component implements IPageRenderer, IPageEditor, IToolContainer {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private Graphics2D _backGraphics = null;
	private Graphics2D _frontGraphics = null;
	// private BufferedImage _backBuffer = null;
	private Image _backBuffer = null;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	private Dimension _lastRenderDimensions = new Dimension();

	private final PagePenDispatcher _pagePenListener = new PagePenDispatcher();

	private IPage _page = null;

	private Image _frontBuffer;
	
	private final Object _paintSynch = new Object();

	private int _renderWidth = 0;
	private int _renderHeight = 0;
	private float _renderFactorX = 1.0f;
	private float _renderFactorY = 1.0f;

	public JPageRenderer() {
		//setDoubleBuffered(false);

		AwtPenToolkit.addPenListener(this, _pagePenListener);
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);
	}

	@Override
	public void paint(final Graphics g) {
		synchronized (_paintSynch) {
			if (!_lastRenderDimensions.equals(this.getSize())) {
				_lastRenderDimensions = this.getSize();
				if (_backGraphics != null) {
					_backGraphics.dispose();
				}
				if (_frontGraphics != null) {
					_frontGraphics.dispose();
				}
				initializeGraphics();
			}
			g.drawImage(_frontBuffer, 0, 0, null);
		}
	}

	/**
	 * (re)initialized the buffers and drawing destinations
	 */
	private void initializeGraphics() {
		// System.out.println("initGraphics");
		if ((this.getWidth() > 0) && (this.getHeight() > 0) && this.isVisible()) {
			_renderWidth = this.getWidth();
			_renderHeight = this.getHeight();
			_backBuffer = createImage(_renderWidth, _renderHeight);
			_backGraphics = (Graphics2D) _backBuffer.getGraphics();
			_backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			_backGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_NORMALIZE);
			_backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			_backGraphics.setRenderingHint(
					RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			_frontBuffer = createImage(_renderWidth, _renderHeight);
			_frontGraphics = (Graphics2D) _frontBuffer.getGraphics();
			_frontGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED);
			_frontGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
			_frontGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			_frontGraphics.setRenderingHint(
					RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			_renderFactorX = _renderWidth;
			_renderFactorY = _renderHeight;

			redrawBack();

			// Update pen listener
			_pagePenListener.setDrawSize(new Dimension(_renderWidth,
					_renderHeight));
		}
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		if (_backGraphics != null) {
			_backGraphics.setPaint(pen.getColor());
			_backGraphics.setStroke(pen.getStroke());
			final Shape transformedPath = path
					.createTransformedShape(AffineTransform.getScaleInstance(
							_renderFactorX, _renderFactorY));
			_backGraphics.draw(transformedPath);
		}
	}

	@Override
	public void fill(final Paint paint) {
		if (_backGraphics != null) {
			_backGraphics.setPaint(paint);
			_backGraphics.fillRect(0, 0, _renderWidth, _renderHeight);
		}
	}

	private void repaint(final float x, final float y, final float width,
			final float height, final IPen pen) {
		_frontBuffer.flush();
		final float thickness = pen.getThickness();
		repaint((int) (x - (thickness / 2.0f)) - 1,
				(int) (y - (thickness / 2.0f)) - 1,
				(int) (width + thickness) + 2, (int) (height + thickness) + 2);
	}

	@Override
	public void draw(final IPen pen, final float x, final float y) {
		if (_backGraphics != null) {
			_ellipseRenderer.x = (x * _renderFactorX)
					- (pen.getThickness() / 2.0f);
			_ellipseRenderer.y = (y * _renderFactorY)
					- (pen.getThickness() / 2.0f);
			_ellipseRenderer.width = pen.getThickness();
			_ellipseRenderer.height = pen.getThickness();
			_backGraphics.setPaint(pen.getColor());
			_backGraphics.fill(_ellipseRenderer);
		}
	}

	@Override
	public void drawGridLine(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		if (_backGraphics != null) {
			_lineRenderer.x1 = x1 * _renderFactorX;
			_lineRenderer.y1 = y1 * _renderFactorY;
			_lineRenderer.x2 = x2 * _renderFactorX;
			_lineRenderer.y2 = y2 * _renderFactorY;
			_backGraphics.setStroke(pen.getStroke());
			_frontGraphics.setPaint(pen.getColor());
			_backGraphics.draw(_lineRenderer);
		}
	}

	@Override
	public void drawFront(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		synchronized (_paintSynch) {
			if (_frontGraphics != null) {
				_lineRenderer.x1 = x1 * _renderFactorX;
				_lineRenderer.y1 = y1 * _renderFactorY;
				_lineRenderer.x2 = x2 * _renderFactorX;
				_lineRenderer.y2 = y2 * _renderFactorY;
				_frontGraphics.setStroke(pen.getStroke());
				_frontGraphics.setPaint(pen.getColor());
				_frontGraphics.draw(_lineRenderer);
				final float x = Math.min(_lineRenderer.x1, _lineRenderer.x2);
				final float y = Math.min(_lineRenderer.y1, _lineRenderer.y2);
				final float width = Math.max(_lineRenderer.x1, _lineRenderer.x2)
						- x;
				final float height = Math.max(_lineRenderer.y1, _lineRenderer.y2)
						- y;
				repaint(x, y, width, height, pen);
			}
		}
	}

	@Override
	public void drawFront(final IPen pen, final float x, final float y) {
		synchronized (_paintSynch) {
		if (_frontGraphics != null) {
			_ellipseRenderer.x = (x * _renderFactorX)
					- (pen.getThickness() / 2.0f);
			_ellipseRenderer.y = (y * _renderFactorY)
					- (pen.getThickness() / 2.0f);
			_ellipseRenderer.width = pen.getThickness();
			_ellipseRenderer.height = pen.getThickness();
			_frontGraphics.setPaint(pen.getColor());
			_frontGraphics.fill(_ellipseRenderer);
			repaint(x * _renderFactorX, y * _renderFactorY, 0.0f, 0.0f, pen);
		}
		}
	}

	@Override
	public void clearFront() {
		synchronized (_paintSynch) {
		if (_frontGraphics != null) {
			_backBuffer.flush();
			_frontGraphics.drawImage(_backBuffer, 0, 0, null);
			_frontBuffer.flush();
			repaint();
		}
		}
	}

	/**
	 * Redraw back buffer and clear front
	 */
	public void redrawBack() {
		synchronized (_paintSynch) {
		if (_page == null) {
			if (_backGraphics != null) {
				_backGraphics.setColor(Color.WHITE);
				_backGraphics.fillRect(0, 0, _renderWidth, _renderHeight);
				// System.out.println("clearBack");
			}
		} else {
			_page.render(this);
			// System.out.println("redrawBack");
		}
		clearFront();
		}
	}

	@Override
	public IPage getPage() {
		return _page;
	}

	@Override
	public void setPage(final IPage page) {
		_page = page;
	}

	@Override
	public ITool getNormalTool() {
		return _pagePenListener.getNormalTool();
	}

	@Override
	public void setNormalTool(final ITool normalTool) {
		_pagePenListener.setNormalTool(normalTool);
	}

	@Override
	public ITool getInvertedTool() {
		return _pagePenListener.getInvertedTool();
	}

	@Override
	public void setInvertedTool(final ITool invertedTool) {
		_pagePenListener.setInvertedTool(invertedTool);
	}

	@Override
	public Component getContainer() {
		return this;
	}
}
