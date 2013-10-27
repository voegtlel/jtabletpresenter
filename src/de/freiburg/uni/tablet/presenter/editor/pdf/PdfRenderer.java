package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.common.CSCreator;
import de.intarsys.pdf.encoding.WinAnsiEncoding;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontTools;
import de.intarsys.pdf.font.PDFontType1;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDForm;
import de.intarsys.pdf.pd.PDImage;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.image.awt.ImageConverterAwt2Pdf;
import de.intarsys.tools.locator.FileLocator;

public class PdfRenderer implements IPageBackRenderer {
	private float _renderFactorX = 1;
	private float _renderFactorY = 1;

	private final Ellipse2D.Float _ellipseRenderer = new Ellipse2D.Float();

	private Map<Object, Object> _baseCopiedMap = new HashMap<Object, Object>();
	private PDDocument _pdf;
	private PDPage _page;
	private PDForm _form;
	private PDFont _font;
	
	private int _pageIndex = 0;
	private CSCreator _creator;
	
	private File _file;
	private CDSRectangle _pageSize;
	private boolean _showPageNumber;
	
	private boolean _wasEmptyPage = true;
	private boolean _ignoreEmptyPage;
	private float _thicknessFactor;
	private boolean _ignoreEmptyPageNumber;
	
	public PdfRenderer(final File file, final float screenSizeX, final float screenSizeY, final boolean ignoreEmptyPage, final boolean ignoreEmptyPageNumber, final boolean showPageNumber, final float thicknessFactor) throws Exception {
		_ignoreEmptyPage = ignoreEmptyPage;
		_ignoreEmptyPageNumber = ignoreEmptyPageNumber;
		_showPageNumber = showPageNumber;
		_thicknessFactor = thicknessFactor;
		System.out.println("create pdf");
		_pdf = PDDocument.createNew();
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
			// Finally add page number
			if (_showPageNumber && (!_wasEmptyPage || !_ignoreEmptyPageNumber)) {
				// Draw page number
				float fontSize = 20;
				_creator.textSetFont(null, _font, fontSize);
				_pageIndex++;
				String s = Integer.valueOf(_pageIndex).toString();
				
				byte[] data = s.getBytes(Charset.forName("windows-1252"));
				float sWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, fontSize, data, 0, data.length);
				_creator.textMoveTo(_renderFactorX - sWidth - 10, 12);
				_creator.textShow(s);
				
				_wasEmptyPage = false;
			} else if (_showPageNumber && (_form != null)) {
				_pageIndex++;
			}
			System.out.println("creator close");
			_creator.close();
			if ((_form != null) && !_wasEmptyPage) {
				_form.setBoundingBox(_pageSize);
				System.out.println("overlay form");
				AffineTransform transform = PdfPageHelper.calculateTransform(_page);
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
			_wasEmptyPage = _ignoreEmptyPage;
		}
	}
	
	public void nextPage(final PdfPageSerializable page) throws Exception {
		if (_creator != null && _wasEmptyPage && _ignoreEmptyPage && _form == null) {
			return;
		}
		endPage();
		if (page != null) {
			final PDPage sourcePage = page.getPage();
			System.out.println("copy page");
			_page = (PDPage) PDPage.META.createFromCos(sourcePage.cosGetObject().copyDeep(_baseCopiedMap));
			_pdf.addPageNode(_page);
		} else {
			_page = null;
		}
		if (_page == null) {
			System.out.println("new page");
			_form = null;
			_pageSize = _pageSize.copy();
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
			final PathIterator pathIterator = path.getPathIterator(AffineTransform.getScaleInstance(
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
	public void draw(final BufferedImage image, final float x, final float y, final float width, final float height) {
		try {
			final BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			final Graphics g = rgbImage.getGraphics();
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();
			final ImageConverterAwt2Pdf converter = new ImageConverterAwt2Pdf(rgbImage);
			converter.setPreferJpeg(true);
			final PDImage pdImage = converter.getPDImage();
			_creator.saveState();
			_creator.transform(width * _renderFactorX, 0, 0, height * _renderFactorY, x * _renderFactorX, (1f - y - height) * _renderFactorY );
			_creator.doXObject(null, pdImage);
			_creator.restoreState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_wasEmptyPage = false;
	}
	
	@Override
	public void draw(final IPen pen, final float x1, final float y1, final float x2, final float y2) {
		throw new IllegalStateException("Can't draw line in pdf");
	}
	
	@Override
	public void draw(final float x, final float y, final String text, final TextFont font) {
		// Draw page number
		_creator.textSetFont(null, font.getPDFont(), font.getSize());
		//byte[] data = font.getEncodedTextData(text);
		_creator.textMoveTo(x, y);
		//_creator.textShow(data, 0, data.length);
		_creator.textShow(text);
	}
	
	@Override
	public void requireRepaint() {
		throw new IllegalStateException("Can't repaint pdf");
	}
	
	@Override
	public void requireRepaint(final IRenderable renderable, final boolean clear) {
		throw new IllegalStateException("Can't repaint pdf");
	}

	@Override
	public void setRepaintListener(final IPageRepaintListener repaintListener) {
		// Ignore
	}
	
	@Override
	public void setOffset(final float x, final float y) {
		throw new IllegalStateException("Can't pdf");
	}
}
