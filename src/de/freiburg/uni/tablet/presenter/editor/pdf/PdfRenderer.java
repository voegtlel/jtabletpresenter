package de.freiburg.uni.tablet.presenter.editor.pdf;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import de.freiburg.uni.tablet.presenter.document.BitmapImageData;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.common.CSCreator;
import de.intarsys.pdf.cos.COSIndirectObject;
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
	
	private Map<PDFont, PDFont> _fontCopyMap = new HashMap<PDFont, PDFont>();
	
	private int _pageIndex = 0;
	private CSCreator _creator;
	
	private File _file;
	private CDSRectangle _pageSize;
	private CDSRectangle _pageSizeOrig;
	private boolean _showPageNumber;
	
	private boolean _wasEmptyPage = true;
	private boolean _ignoreEmptyPage;
	private boolean _ignoreEmptyPageNumber;
	private boolean _ignorePdfPageNumber;
	
	private RenderMetric _penRenderMetric = new RenderMetric();
	
	public PdfRenderer(final File file, final float screenSizeX, final float screenSizeY, final boolean ignoreEmptyPage, final boolean ignoreEmptyPageNumber, final boolean ignorePdfPageNumber, final boolean showPageNumber, final float thicknessFactor) throws Exception {
		_ignoreEmptyPage = ignoreEmptyPage;
		_ignoreEmptyPageNumber = ignoreEmptyPageNumber;
		_ignorePdfPageNumber = ignorePdfPageNumber;
		_showPageNumber = showPageNumber;
		System.out.println("create pdf");
		_pdf = PDDocument.createNew();
		_pageSize = new CDSRectangle(0f, 0f, screenSizeX, screenSizeY);
		_pageSizeOrig = _pageSize;
		_file = file;
		if (_showPageNumber) {
			_font = PDFontType1.createNew(PDFontType1.FONT_Helvetica);
			_font.setEncoding(WinAnsiEncoding.UNIQUE);
		}
		
		_penRenderMetric.setStroke(false, thicknessFactor);
		_penRenderMetric.update((int)screenSizeX, (int)screenSizeY, null);
	}
	
	public void close() throws IOException {
		endPage();
		System.out.println("save, close");
		_pdf.save(new FileLocator(_file));
		_pdf.close();
	}
	
	private void endPage() {
		if (_creator != null) {
			// Finally add page number
			if (_showPageNumber && (!_wasEmptyPage || !_ignoreEmptyPageNumber) && (!_ignorePdfPageNumber || _form == null)) {
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
				AffineTransform transform;
				try {
					transform = PdfPageHelper.calculateTransform(_page);
				} catch(Exception e) {
					System.err.println("Can't prerender page, skipping");
					_wasEmptyPage = true;
					return;
				}
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
	
	public void nextPage(final IEntity pageBackgroundEntity) throws Exception {
		if (_creator != null && _wasEmptyPage && _ignoreEmptyPage && _form == null) {
			return;
		}
		endPage();
		_pageSize = null;
		if (pageBackgroundEntity != null) {
			if (pageBackgroundEntity instanceof PdfPageSerializable) {
				final PDPage sourcePage = ((PdfPageSerializable)pageBackgroundEntity).getPage();
				System.out.println("copy page");
				_page = (PDPage) PDPage.META.createFromCos(sourcePage.cosGetObject().copyDeep(_baseCopiedMap));
				_pdf.addPageNode(_page);
			} else if (pageBackgroundEntity instanceof BitmapImageData) {
				BufferedImage image = ((BitmapImageData) pageBackgroundEntity).getImage();
				_pageSize = new CDSRectangle(0, 0, image.getWidth(), image.getHeight());
				_page = null;
			} else {
				throw new IllegalArgumentException("Invalid entity");
			}
		} else {
			_page = null;
		}
		if (_page == null) {
			System.out.println("new page");
			_form = null;
			if (_pageSize == null) {
				_pageSize = _pageSizeOrig.copy();
			}
			_page = (PDPage) PDPage.META.createNew();
			_page.setCropBox(_pageSize);
			_page.setMediaBox(_pageSize);
			_pdf.addPageNode(_page);
			System.out.println("creator from page");
			_creator = CSCreator.createNew(_page);
		} else {
			_pageSize = _page.getMediaBox().copy();
			_pageSizeOrig = _pageSize;
			_form = (PDForm) PDForm.META.createNew();
			_form.setBoundingBox(_pageSize);
			System.out.println("creator from form");
			_creator = CSCreator.createNew(_form);
		}
		_creator.setLineCap(1);
		_creator.setLineJoin(1);
		_renderFactorX = _pageSize.getWidth();
		_renderFactorY = _pageSize.getHeight();
		if (pageBackgroundEntity instanceof BitmapImageData) {
			BufferedImage image = ((BitmapImageData) pageBackgroundEntity).getImage();
			draw(image, 0, 0, 1, 1);
		}
	}
	
	@Override
	public void draw(final IPen pen, final float x,
			final float y) {
		float thickness = pen.getThickness(_penRenderMetric, IPen.DEFAULT_PRESSURE);
		_ellipseRenderer.x = x * _renderFactorX - thickness / 2.0f;
		_ellipseRenderer.y = _renderFactorY - y * _renderFactorY - thickness / 2.0f;
		_ellipseRenderer.width = thickness;
		_ellipseRenderer.height = thickness;
		try {
			_creator.setNonStrokeColorRGB(pen.getColor().getRed()/255f, pen.getColor().getGreen()/255f, pen.getColor().getBlue()/255f);
			_creator.penCircle(_ellipseRenderer.x + _ellipseRenderer.width / 2, _ellipseRenderer.y + _ellipseRenderer.height / 2, thickness / 2);
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
			_creator.setLineWidth(pen.getThickness(_penRenderMetric, IPen.DEFAULT_PRESSURE));
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
	
	private IPen _pathPen;
	private boolean _isFirstPath = false;
	private boolean _captureThickness = true;
	
	@Override
	public void beginPath(final IPen pen) {
		_pathPen = pen;
		_creator.setStrokeColorRGB(pen.getColor().getRed()/255f, pen.getColor().getGreen()/255f, pen.getColor().getBlue()/255f);
		if (!_captureThickness) {
			_creator.setLineWidth(pen.getThickness(_penRenderMetric, IPen.DEFAULT_PRESSURE));
		}
		_isFirstPath = true;
	}
	
	@Override
	public void endPath() {
		if (!_captureThickness) {
			_creator.pathStroke();
		}
		_pathPen = null;
	}
	
	@Override
	public void drawPath(final float x1, final float y1, final float x2, final float y2,
			final float pressure) {
		if (_captureThickness) {
			_creator.setLineWidth(_pathPen.getThickness(_penRenderMetric, pressure));
		}
		float x1t = x1 * _renderFactorX;
		float y1t = _renderFactorY - y1 * _renderFactorY;
		float x2t = x2 * _renderFactorX;
		float y2t = _renderFactorY - y2 * _renderFactorY;
		if (_captureThickness || _isFirstPath) {
			_creator.penMoveTo(x1t, y1t);
			_isFirstPath = false;
		}
		_creator.penLineTo(x2t, y2t);
		_creator.pathStroke();
		_wasEmptyPage = false;
	}
	
	@Override
	public void draw(final BufferedImage image, final float x, final float y, final float width, final float height) {
		try {
			final BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
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
	public void draw(final float x, final float y, final String[] textLines, final TextFont font) {
		// Draw string
		System.out.println("draw " + textLines[0] + "...");
		PDFont docFont;
		if (!_fontCopyMap.containsKey(font.getPDFont())) {
			docFont = (PDFont)PDFont.META.createFromCos(COSIndirectObject.create(font.getPDFont().cosGetObject().copyDeep()).dereference());
			_fontCopyMap.put(font.getPDFont(), docFont);
		} else {
			docFont = _fontCopyMap.get(font.getPDFont());
		}
		_creator.saveState();
		_creator.transform(_renderFactorX, 0, 0, _renderFactorY, x * _renderFactorX, (1.0f-y) * _renderFactorY );
		_creator.textSetFont(null, docFont, font.getSize());
		for (int i = 0; i < textLines.length; i++) {
			_creator.textShow(textLines[i]);
			if (i < textLines.length - 1) {
				_creator.textLineMove(0, -font.getLineHeight());
			}
		}
		_creator.restoreState();
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
	
	@Override
	public void drawDebugRect(final float minX, final float minY, final float maxX, final float maxY) {
		throw new IllegalStateException("Can't pdf");
	}
}
