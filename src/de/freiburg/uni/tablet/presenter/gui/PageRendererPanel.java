package de.freiburg.uni.tablet.presenter.gui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PageRendererPanel extends JPanel implements IPageRenderer,
		IPageFrontRenderer {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private Graphics2D _backGraphics;
	private Graphics2D _frontGraphics;
	private BufferedImage _backBuffer;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	public PageRendererPanel() {
		initializeGraphics();
		// TODO: Size Changed listener to resize bitmap
	}

	private void initializeGraphics() {
		_backBuffer = new BufferedImage(this.getWidth(), this.getHeight(),
				BufferedImage.TYPE_INT_BGR);

		_backGraphics = _backBuffer.createGraphics();

		_backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		_backGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		_backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		_backGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		_frontGraphics = (Graphics2D) this.getGraphics();
		_backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		_backGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		_backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		_backGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		_backGraphics.setPaint(pen.getPaint());
		_backGraphics.setStroke(pen.getStroke());
		_backGraphics.draw(path);
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
		drawPoint(pen, x, y, _backGraphics);
	}

	@Override
	public void drawGridLine(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		_backGraphics.setPaint(pen.getPaint());
		_backGraphics.setStroke(pen.getStroke());
		_lineRenderer.x1 = x1;
		_lineRenderer.y1 = y1;
		_lineRenderer.x2 = x2;
		_lineRenderer.y2 = y2;
		_backGraphics.draw(_lineRenderer);
	}

	@Override
	public void drawFront(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		_frontGraphics.setPaint(pen.getPaint());
		_frontGraphics.setStroke(pen.getStroke());
		_lineRenderer.x1 = x1;
		_lineRenderer.y1 = y1;
		_lineRenderer.x2 = x2;
		_lineRenderer.y2 = y2;
		_frontGraphics.draw(_lineRenderer);
	}

	@Override
	public void drawFront(final IPen pen, final float x, final float y) {
		drawPoint(pen, x, y, _frontGraphics);
	}

	@Override
	public void clearFront() {
		_frontGraphics.drawImage(_backBuffer, 0, 0, null);
	}
}
