package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.editor.IRepaintListener;
import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class PageLayerBufferPdf implements IPageLayerBuffer {
	private IRepaintListener _repaintListener = null;
	
	protected Image _imageBuffer = null;

	protected int _sizeX = 1;
	protected int _sizeY = 1;
	
	protected float _renderFactorX = 1;
	protected float _renderFactorY = 1;

	private int _currentPageIndex = 0;
	private PDPage _page;
	private IDisplayRenderer _displayRenderer;

	private PDPageTree _pageTree;

	private Graphics2D _graphics;

	private Document _document;
	
	private DocumentListener _listener;
	
	/**
	 * Create an empty pdf renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferPdf(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
		if (_displayRenderer.isWorking()) {
			resize(_displayRenderer.getWidth(), _displayRenderer.getHeight());
		}
		_listener = new DocumentAdapter() {
			@Override
			public void pdfChanged(final PdfSerializable lastPdf) {
				PageLayerBufferPdf.this.onPdfChanged();
			}
		};
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
	
	/**
	 * Create the backbuffer
	 */
	private void createImage() {
		if (_graphics != null) {
			_graphics.dispose();
		}
		final int imgWidth = Math.max(_sizeX, 1);
		final int imgHeight = Math.max(_sizeY, 1);
		_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
				Transparency.TRANSLUCENT);
		_graphics = (Graphics2D) _imageBuffer.getGraphics();
		System.out.println("Create Pdf Image: " + _sizeX + "x" + _sizeY);
	}
	
	/**
	 * Destroy the back-buffer
	 */
	private void destroyBuffer() {
		if (_imageBuffer != null) {
			if (_graphics != null) {
				_graphics.dispose();
				_graphics = null;
			}
			_imageBuffer = null;
		}
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		if (_imageBuffer != null) {
			g.drawImage(_imageBuffer, 0, 0, obs);
		}
	}
	
	/**
	 * Sets the document
	 * @param document
	 */
	public void setDocument(final Document document) {
		if (_document != null) {
			_document.removeListener(_listener);
		}
		_document = document;
		if (_document != null) {
			System.out.println("Document changed to " + _document.getId());
			_document.addListener(_listener);
		}
		onPdfChanged();
	}

	/**
	 * Event
	 * @param lastPdf
	 */
	protected void onPdfChanged() {
		if ((_document == null) || (_document.getPdf().getDocument() == null)) {
			System.out.println("PDF changed to null");
			destroyBuffer();
		} else {
			System.out.println("PDF changed to " + _document.getPdf().getId());
			_pageTree = _document.getPdf().getDocument().getPageTree();
			_page = _pageTree.getPageAt(_currentPageIndex);
			if (_imageBuffer == null) {
				createImage();
			}
			renderPdf();
		}
	}
	
	/**
	 * Sets the page index
	 * @param index
	 */
	public void setPageIndex(int index) {
		_currentPageIndex = index;
		System.out.println("Pdf page index " + index);
		if ((_pageTree != null) && (index >= 0) && (index < _pageTree.getCount())) {
			_page = _pageTree.getPageAt(index);
			renderPdf();
		} else {
			_page = null;
			renderPdf();
		}
	}
	
	/**
	 * Renders the active pdf page
	 */
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
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Can't render Pdf");
				}
			} else {
				System.out.println("Don't Render Pdf");
			}
		}
		if (_repaintListener != null) {
			_repaintListener.render();
		}
	}

	@Override
	public void clear() {
	}

	public void setRepaintListener(final IRepaintListener repaintListener) {
		_repaintListener = repaintListener;
	}
}
