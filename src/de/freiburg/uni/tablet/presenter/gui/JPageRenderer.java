package de.freiburg.uni.tablet.presenter.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import jpen.owner.multiAwt.AwtPenToolkit;
import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.PageProperties;
import de.freiburg.uni.tablet.presenter.page.RedrawEvent;
import de.freiburg.uni.tablet.presenter.page.RedrawListener;

public class JPageRenderer extends JComponent implements IPageRenderer,
		IPageFrontRenderer {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private Graphics2D _backGraphics = null;
	private Graphics2D _frontGraphics = null;
	private BufferedImage _backBuffer = null;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	private Dimension _lastRenderDimensions = new Dimension();

	private PageProperties _properties = new PageProperties();

	public JPageRenderer() {
		setDoubleBuffered(false);

		// TODO: Size changed listener to resize bitmap

		AwtPenToolkit.addPenListener(this, new PagePenListener());
		AwtPenToolkit.getPenManager().pen.levelEmulator
				.setPressureTriggerForLeftCursorButton(0.5f);
	}

	@Override
	protected void paintComponent(final Graphics g) {
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
		if (_backBuffer != null) {
			g.drawImage(_backBuffer, 0, 0, null);
		} else {
			g.setColor(_properties.getBackgroundColor());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
	}

	@Override
	public void addRedrawListener(final RedrawListener l) {
		this.listenerList.add(RedrawListener.class, l);
	}

	@Override
	public void removeRedrawListener(final RedrawListener l) {
		this.listenerList.remove(RedrawListener.class, l);
	}

	protected void fireRedraw() {
		RedrawEvent args = null;
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RedrawListener.class) {
				if (args == null) {
					args = new RedrawEvent(this, this);
				}
				((RedrawListener) listeners[i + 1]).redraw(args);
			}
		}
	}

	private void initializeGraphics() {
		if ((this.getWidth() > 0) && (this.getHeight() > 0) && this.isVisible()) {
			_backBuffer = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_BGR);

			_backGraphics = _backBuffer.createGraphics();

			_backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			_backGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
			_backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			_backGraphics.setRenderingHint(
					RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			_backGraphics.setColor(_properties.getBackgroundColor());
			_backGraphics.fillRect(0, 0, _backBuffer.getWidth(),
					_backBuffer.getHeight());

			fireRedraw();

			_frontGraphics = (Graphics2D) this.getGraphics();
			if (_frontGraphics != null) {
				_frontGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_SPEED);
				_frontGraphics.setRenderingHint(
						RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
				_frontGraphics.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF);
				_frontGraphics.setRenderingHint(
						RenderingHints.KEY_FRACTIONALMETRICS,
						RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				clearFront();
			}
		}
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		if (_backGraphics != null) {
			_backGraphics.setPaint(pen.getPaint());
			_backGraphics.setStroke(pen.getStroke());
			_backGraphics.draw(path);
		}
	}

	private void drawPoint(final IPen pen, final float x, final float y,
			final Graphics2D dest) {
		_ellipseRenderer.x = x - (pen.getThickness() / 2.0f);
		_ellipseRenderer.y = y - (pen.getThickness() / 2.0f);
		_ellipseRenderer.width = pen.getThickness();
		_ellipseRenderer.height = pen.getThickness();
		dest.setPaint(pen.getPaint());
		dest.setStroke(pen.getStroke());
		dest.fill(_ellipseRenderer);
	}

	@Override
	public void draw(final IPen pen, final float x, final float y) {
		if (_backGraphics != null) {
			drawPoint(pen, x, y, _backGraphics);
		}
	}

	@Override
	public void drawGridLine(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		if (_backGraphics != null) {
			_backGraphics.setPaint(pen.getPaint());
			_backGraphics.setStroke(pen.getStroke());
			_lineRenderer.x1 = x1;
			_lineRenderer.y1 = y1;
			_lineRenderer.x2 = x2;
			_lineRenderer.y2 = y2;
			_backGraphics.draw(_lineRenderer);
		}
	}

	@Override
	public void drawFront(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		if (_frontGraphics != null) {
			_frontGraphics.setPaint(pen.getPaint());
			_frontGraphics.setStroke(pen.getStroke());
			_lineRenderer.x1 = x1;
			_lineRenderer.y1 = y1;
			_lineRenderer.x2 = x2;
			_lineRenderer.y2 = y2;
			_frontGraphics.draw(_lineRenderer);
		}
	}

	@Override
	public void drawFront(final IPen pen, final float x, final float y) {
		if (_frontGraphics != null) {
			drawPoint(pen, x, y, _frontGraphics);
		}
	}

	@Override
	public void clearFront() {
		if (_frontGraphics != null) {
			_frontGraphics.drawImage(_backBuffer, 0, 0, null);
		}
	}

	public PageProperties getProperties() {
		return _properties;
	}

	public void setProperties(final PageProperties properties) {
		_properties = properties;
	}
}
