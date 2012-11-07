package de.freiburg.uni.tablet.presenter.editor.pdf;

import gnu.jpdf.PDFJob;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.print.PageFormat;
import java.io.OutputStream;

import de.freiburg.uni.tablet.presenter.editor.PageRepaintListener;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PdfRenderer implements IPageBackRenderer {
	private float _renderFactorX = 1;
	private float _renderFactorY = 1;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	private PDFJob _job;
	private Graphics2D _graphics;
	
	public PdfRenderer(OutputStream stream) {
		_job = new PDFJob(stream);
	}
	
	public void close() {
		if (_graphics != null) {
			_graphics.dispose();
			_graphics = null;
		}
		_job.end();
	}
	
	public void nextPage() {
		if (_graphics != null) {
			_graphics.dispose();
		}
		_graphics = (Graphics2D) _job.getGraphics(PageFormat.LANDSCAPE);
		_renderFactorX = (float) _job.getCurrentPage().getDimension().getWidth();
		_renderFactorY = (float) _job.getCurrentPage().getDimension().getHeight();
	}
	
	@Override
	public void drawGridLine(final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		_lineRenderer.x1 = x1 * _renderFactorX;
		_lineRenderer.y1 = y1 * _renderFactorY;
		_lineRenderer.x2 = x2 * _renderFactorX;
		_lineRenderer.y2 = y2 * _renderFactorY;
		_graphics.setStroke(pen.getStroke());
		_graphics.setPaint(pen.getColor());
		_graphics.draw(_lineRenderer);
	}

	@Override
	public void draw(final IPen pen, final float x,
			final float y) {
		_ellipseRenderer.x = x * _renderFactorX - pen.getThickness() / 2.0f;
		_ellipseRenderer.y = y * _renderFactorY - pen.getThickness() / 2.0f;
		_ellipseRenderer.width = pen.getThickness();
		_ellipseRenderer.height = pen.getThickness();
		_graphics.setPaint(pen.getColor());
		_graphics.fill(_ellipseRenderer);
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		_graphics.setPaint(pen.getColor());
		_graphics.setStroke(pen.getStroke());
		final Shape transformedPath = path
				.createTransformedShape(AffineTransform.getScaleInstance(
						_renderFactorX, _renderFactorY));
		_graphics.draw(transformedPath);
	}

	@Override
	public void clear() {
		throw new IllegalStateException("Can't clear pdf");
	}

	@Override
	public void setRepaintListener(PageRepaintListener repaintListener) {
		// Ignore
	}
}
