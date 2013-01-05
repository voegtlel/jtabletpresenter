package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import de.intarsys.tools.locator.FileLocator;

public class PageLayerBufferPdf implements IPageLayerBuffer {
	protected Image _imageBuffer = null;

	protected int _sizeX = 1;
	protected int _sizeY = 1;
	
	protected float _renderFactorX = 1;
	protected float _renderFactorY = 1;

	private PDPage _page;
	private IDisplayRenderer _displayRenderer;

	private PDDocument _doc;

	private PDPageTree _pageTree;

	private Graphics2D _graphics;
	
	public PageLayerBufferPdf(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
		if (_displayRenderer.isWorking()) {
			resize(_displayRenderer.getWidth(), _displayRenderer.getHeight());
		}
	}
	
	@Override
	public void resize(final int width, final int height) {
		_sizeX = width;
		_sizeY = height;
		_renderFactorX = width;
		_renderFactorY = height;

		if (_page != null) {
			createImage();
			renderPdf();
		}
	}
	
	private void createImage() {
		final int imgWidth = Math.max(_sizeX, 1);
		final int imgHeight = Math.max(_sizeY, 1);
		_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
				Transparency.TRANSLUCENT);
		_graphics = (Graphics2D) _imageBuffer.getGraphics();
		System.out.println("Create Pdf Image: " + _sizeX + "x" + _sizeY);
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		if (_imageBuffer != null) {
			g.drawImage(_imageBuffer, 0, 0, obs);
		}
	}
	
	public void setDocument(final File file) throws IOException {
		try {
			setDocument(PDDocument.createFromLocator(new FileLocator(file)));
		} catch (COSLoadException e) {
			throw new IOException(e);
		}
	}

	public void setDocument(final PDDocument doc) {
		_doc = doc;
		_pageTree = doc.getPageTree();
		_page = _pageTree.getFirstPage();
		if (_imageBuffer == null) {
			createImage();
		}
		renderPdf();
	}
	
	public PDDocument getDocument() {
		return _doc;
	}
	
	public void setPageIndex(int index) {
		if (_pageTree != null) {
			_page = _pageTree.getPageAt(index);
			renderPdf();
		}
	}
	
	private void renderPdf() {
		if (_graphics != null) {
			Composite defaultComp = _graphics.getComposite();
			AffineTransform defaultTransform = new AffineTransform();
			_graphics.setTransform(defaultTransform);
			_graphics.setComposite(AlphaComposite.Clear); 
			_graphics.setColor(new Color(0, 0, 0, 0));
			_graphics.fillRect(0, 0, _sizeX, _sizeY);
			_graphics.setComposite(defaultComp);
			if (_page != null) {
				AffineTransform transform = new AffineTransform();
				transform.translate(0, _renderFactorY);
				transform.scale(_renderFactorX/_page.getMediaBox().getWidth(), -_renderFactorY/_page.getMediaBox().getHeight());
				_graphics.setTransform(transform);
				System.out.println("Init Render Pdf");
				IGraphicsContext graphics = new CwtAwtGraphicsContext(_graphics);
				CSContent content = _page.getContentStream();
				if (content != null) {
					System.out.println("Render Pdf...");
					CSPlatformRenderer renderer = new CSPlatformRenderer(null, graphics);
					renderer.process(content, _page.getResources());
					System.out.println("translate after: " + graphics.getTransform().getTranslateX() + ", " + graphics.getTransform().getTranslateY());
					System.out.println("scale after: " + graphics.getTransform().getScaleX() + ", " + graphics.getTransform().getScaleY());
				}
				System.out.println("Rendered Pdf");
			} else {
				System.out.println("Don't Render Pdf");
			}
		}
	}

	@Override
	public void clear() {
	}
}
