package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.OutputStream;

import com.pdfjet.Cap;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Join;
import com.pdfjet.Letter;
import com.pdfjet.PDF;
import com.pdfjet.Page;

import de.freiburg.uni.tablet.presenter.editor.PageRepaintListener;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PdfRenderer implements IPageBackRenderer {
	private float _renderFactorX = 1;
	private float _renderFactorY = 1;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	private PDF _pdf;
	private Page _page;
	private Font _font;
	
	private int _pageIndex = 0;
	
	public PdfRenderer(OutputStream stream) throws Exception {
		_pdf = new PDF(stream);
		_font = new Font(_pdf, CoreFont.HELVETICA);
	}
	
	public void close() {
		try {
			_pdf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void nextPage() throws Exception {
		_page = new Page(_pdf, Letter.LANDSCAPE);
		_renderFactorX = _page.getWidth();
		_renderFactorY = _page.getHeight();
		
		// Draw page number
		_pageIndex++;
		String s = Integer.valueOf(_pageIndex).toString();
		float sWidth = _font.stringWidth(s);
		_page.drawString(_font, s, _page.getWidth() - sWidth - _font.getHeight() / 2, _page.getHeight() - _font.getDescent() - _font.getHeight() / 2);
	}
	
	@Override
	public void drawGridLine(final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		_lineRenderer.x1 = x1 * _renderFactorX;
		_lineRenderer.y1 = y1 * _renderFactorY;
		_lineRenderer.x2 = x2 * _renderFactorX;
		_lineRenderer.y2 = y2 * _renderFactorY;
		try {
			_page.setPenColor(pen.getColor().getRGB());
			_page.setPenWidth(pen.getThickness());
			_page.setLineCapStyle(Cap.ROUND);
			_page.setLineJoinStyle(Join.ROUND);
			_page.drawLine(_lineRenderer.x1, _lineRenderer.y1, _lineRenderer.x2, _lineRenderer.y2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(final IPen pen, final float x,
			final float y) {
		_ellipseRenderer.x = x * _renderFactorX - pen.getThickness() / 2.0f;
		_ellipseRenderer.y = y * _renderFactorY - pen.getThickness() / 2.0f;
		_ellipseRenderer.width = pen.getThickness();
		_ellipseRenderer.height = pen.getThickness();
		try {
			_page.setBrushColor(pen.getColor().getRGB());
			_page.fillEllipse(_ellipseRenderer.x + _ellipseRenderer.width / 2, _ellipseRenderer.y + _ellipseRenderer.height / 2, _ellipseRenderer.width / 2, _ellipseRenderer.height / 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		try {
			_page.setPenColor(pen.getColor().getRGB());
			_page.setPenWidth(pen.getThickness());
			_page.setLineCapStyle(Cap.ROUND);
			_page.setLineJoinStyle(Join.ROUND);
			PathIterator pathIterator = path.getPathIterator(AffineTransform.getScaleInstance(
					_renderFactorX, _renderFactorY));
			double[] coords = new double[6];
			while (!pathIterator.isDone()) {
				int type = pathIterator.currentSegment(coords);
				if (type == PathIterator.SEG_MOVETO) {
					_page.moveTo(coords[0], coords[1]);
				} else if (type == PathIterator.SEG_LINETO) {
					_page.lineTo(coords[0], coords[1]);
				} else if (type == PathIterator.SEG_QUADTO) {
					_page.lineTo(coords[0], coords[1]);
				} else if (type == PathIterator.SEG_CUBICTO) {
					_page.lineTo(coords[0], coords[1]);
				}
				pathIterator.next();
			}
			_page.strokePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
