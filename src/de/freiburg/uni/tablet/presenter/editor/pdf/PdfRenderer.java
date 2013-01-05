package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.nio.charset.Charset;

import de.freiburg.uni.tablet.presenter.editor.PageRepaintListener;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.CSOperator;
import de.intarsys.pdf.content.CSOperators;
import de.intarsys.pdf.content.common.CSCreator;
import de.intarsys.pdf.cos.COSObject;
import de.intarsys.pdf.encoding.WinAnsiEncoding;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontTools;
import de.intarsys.pdf.font.PDFontType1;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDForm;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDResources;
import de.intarsys.tools.locator.FileLocator;

public class PdfRenderer implements IPageBackRenderer {
	private float _renderFactorX = 1;
	private float _renderFactorY = 1;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();
	private final Line2D.Float _lineRenderer = new Line2D.Float();

	private PDDocument _pdf;
	private PDPage _page;
	private PDForm _form;
	private PDFont _font;
	
	private int _pageIndex = 0;
	private CSCreator _creator;
	
	private File _file;
	private CDSRectangle _pageSize;
	private boolean _showPageNumber = true;
	
	private PDDocument _baseDocument;
	
	private boolean _wasEmptyPage = true;
	private boolean _ignoreEmptyPage;
	private float _thicknessFactor;
	
	public PdfRenderer(File file, float screenSizeX, float screenSizeY, PDDocument baseDocument, boolean ignoreEmptyPage, float thicknessFactor) throws Exception {
		_ignoreEmptyPage = ignoreEmptyPage;
		_thicknessFactor = thicknessFactor;
		if (baseDocument != null) {
			_baseDocument = baseDocument;
			System.out.println("copy pdf");
			_pdf = baseDocument.copyDeep();
		} else {
			System.out.println("create pdf");
			_pdf = PDDocument.createNew();
		}
		_pageSize = new CDSRectangle(0f, 0f, screenSizeX, screenSizeY);
		_file = file;
		if (_showPageNumber) {
			_font = PDFontType1.createNew(PDFontType1.FONT_Helvetica);
			_font.setEncoding(WinAnsiEncoding.UNIQUE);
		}
	}
	
	public void close() {
		try {
			endPage();
			System.out.println("save, close");
			_pdf.save(new FileLocator(_file));
			_pdf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void endPage() {
		if (_creator != null) {
			System.out.println("creator close");
			_creator.close();
			if ((_form != null) && !_wasEmptyPage) {
				_form.setBoundingBox(_pageSize);
				System.out.println("overlay form");
				AffineTransform transform = PdfPageRedrawer.calculateTransform2(_page);
				try {
					transform.invert();
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
				CSContent content = CSContent.createNew();
				CSCreator creator = CSCreator.createFromContent(content, _page);
				creator.saveState();
				creator.transform((float)transform.getScaleX(), (float)transform.getShearX(), (float)transform.getShearY(),
						(float)transform.getScaleY(), (float)transform.getTranslateX(), (float)transform.getTranslateY());
				creator.doXObject(null, _form);
				creator.restoreState();
				creator.close();
				_page.cosAddContents(content.createStream());
			}
			_wasEmptyPage = true;
		}
	}
	
	public void nextPage() throws Exception {
		if (_creator != null && _wasEmptyPage && _ignoreEmptyPage && _form != null) {
			return;
		}
		endPage();
		if ((_creator == null) && (_baseDocument != null)) {
			_page = _pdf.getPageTree().getFirstPage();
			System.out.println("first page");
		} else if (_baseDocument != null) {
			_page = _page.getNextPage();
			System.out.println("next page");
		} else {
			_page = null;
		}
		if (_page == null) {
			System.out.println("new page");
			_form = null;
			_page = (PDPage) PDPage.META.createNew();
			_page.setCropBox(_pageSize);
			_page.setMediaBox(_pageSize);
			_pdf.addPageNode(_page);
			System.out.println("creator from page");
			_creator = CSCreator.createNew(_page);
		} else {
			_pageSize = _page.getMediaBox().copy();
			_form = (PDForm) PDForm.META.createNew();
			_form.setBoundingBox(_pageSize);
			System.out.println("creator from form");
			_creator = CSCreator.createNew(_form);
		}
		_creator.setLineCap(1);
		_creator.setLineJoin(1);
		_renderFactorX = _pageSize.getWidth();
		_renderFactorY = _pageSize.getHeight();
		
		if (_showPageNumber) {
			// Draw page number
			float fontSize = 20;
			_creator.textSetFont(null, _font, fontSize);
			_pageIndex++;
			String s = Integer.valueOf(_pageIndex).toString();
			
			byte[] data = s.getBytes(Charset.forName("windows-1252"));
			float sWidth = PDFontTools.getGlyphWidthEncoded(_font, data, 0, data.length) * fontSize / 1000f;
			_creator.textMoveTo(_renderFactorX - sWidth - 10, 12);
			_creator.textShow(s);
			
			_wasEmptyPage = false;
		}
	}
	
	@Override
	public void drawGridLine(final IPen pen, final float x1,
			final float y1, final float x2, final float y2) {
		_lineRenderer.x1 = x1 * _renderFactorX;
		_lineRenderer.y1 = _renderFactorY - y1 * _renderFactorY;
		_lineRenderer.x2 = x2 * _renderFactorX;
		_lineRenderer.y2 = _renderFactorY - y2 * _renderFactorY;
		_creator.setStrokeColorRGB(pen.getColor().getRed()/255f, pen.getColor().getGreen()/255f, pen.getColor().getBlue()/255f);
		_creator.setLineWidth(pen.getThickness() * _thicknessFactor);
		_creator.penMoveTo(_lineRenderer.x1, _lineRenderer.y1);
		_creator.penLineTo(_lineRenderer.x2, _lineRenderer.y2);
		_creator.pathStroke();
	}

	@Override
	public void draw(final IPen pen, final float x,
			final float y) {
		_ellipseRenderer.x = x * _renderFactorX - pen.getThickness()  * _thicknessFactor / 2.0f;
		_ellipseRenderer.y = _renderFactorY - y * _renderFactorY - pen.getThickness()  * _thicknessFactor / 2.0f;
		_ellipseRenderer.width = pen.getThickness();
		_ellipseRenderer.height = pen.getThickness();
		try {
			_creator.setNonStrokeColorRGB(pen.getColor().getRed()/255f, pen.getColor().getGreen()/255f, pen.getColor().getBlue()/255f);
			_creator.penCircle(_ellipseRenderer.x + _ellipseRenderer.width / 2, _ellipseRenderer.y + _ellipseRenderer.height / 2, pen.getThickness() / 2);
			_creator.pathFillStrokeEvenOdd();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_wasEmptyPage = false;
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		try {
			_creator.setStrokeColorRGB(pen.getColor().getRed()/255f, pen.getColor().getGreen()/255f, pen.getColor().getBlue()/255f);
			_creator.setLineWidth(pen.getThickness() * _thicknessFactor);
			PathIterator pathIterator = path.getPathIterator(AffineTransform.getScaleInstance(
					_renderFactorX, _renderFactorY));
			double[] coords = new double[6];
			while (!pathIterator.isDone()) {
				int type = pathIterator.currentSegment(coords);
				if (type == PathIterator.SEG_MOVETO) {
					_creator.penMoveTo((float)coords[0], _renderFactorY - (float)coords[1]);
				} else if (type == PathIterator.SEG_LINETO) {
					_creator.penLineTo((float)coords[0], _renderFactorY - (float)coords[1]);
				} else if (type == PathIterator.SEG_QUADTO) {
					_creator.penLineTo((float)coords[0], _renderFactorY - (float)coords[1]);
				} else if (type == PathIterator.SEG_CUBICTO) {
					_creator.penLineTo((float)coords[0], _renderFactorY - (float)coords[1]);
				}
				pathIterator.next();
			}
			_creator.pathStroke();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_wasEmptyPage = false;
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
