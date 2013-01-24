package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class PageLayerBufferPdf implements IPageLayerBuffer {
	protected Image _imageBuffer = null;

	protected int _sizeX = 1;
	protected int _sizeY = 1;
	
	protected float _renderFactorX = 1;
	protected float _renderFactorY = 1;

	private PdfPageSerializable _pdfPage;
	private IDisplayRenderer _displayRenderer;

	private Graphics2D _graphics;
	
	private Object _repaintSync = new Object();
	
	private boolean _requireRepaint = true;
	private boolean _requireResize = true;
	
	/**
	 * Create an empty pdf renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferPdf(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}
	
	@Override
	public void resize(final int width, final int height) {
		synchronized (_repaintSync) {
			_sizeX = width;
			_sizeY = height;
			_renderFactorX = width;
			_renderFactorY = height;
			_requireResize = true;
		}
	}
	
	/**
	 * Sets the document
	 * @param document
	 */
	public void setPdfPage(final PdfPageSerializable pdfPage) {
		synchronized (_repaintSync) {
			_pdfPage = pdfPage;
			_requireRepaint = true;
			_displayRenderer.requireRepaint();
		}
	}
	
	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		boolean requiresResize;
		int width;
		int height;
		float factorX;
		float factorY;
		boolean requiresRepaint;
		PDPage page;
		synchronized (_repaintSync) {
			requiresResize = _requireResize;
			width = _sizeX;
			height = _sizeY;
			factorX = _renderFactorX;
			factorY = _renderFactorY;
			requiresRepaint = _requireRepaint;
			page = ((_pdfPage == null)?null:_pdfPage.getPage());
		}
		if (_pdfPage == null) {
			System.out.println("PDF changed to null");
			
		} else {
			System.out.println("PDF changed to " + _pdfPage.getId());
			
		}
		if (page == null) {
			destroyBuffer();
		} else {
			if (_imageBuffer == null || requiresResize) {
				createImage(width, height);
				requiresRepaint = true;
			}
			if (requiresRepaint) {
				renderPdf(page, width, height, factorX, factorY);
			}
			g.drawImage(_imageBuffer, 0, 0, obs);
		}
	}
	
	/**
	 * Create the backbuffer.
	 * Synchronized to drawBuffer.
	 */
	private void createImage(final int width, final int height) {
		if (_graphics != null) {
			_graphics.dispose();
		}
		final int imgWidth = Math.max(width, 1);
		final int imgHeight = Math.max(height, 1);
		_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
				Transparency.TRANSLUCENT);
		_graphics = (Graphics2D) _imageBuffer.getGraphics();
		System.out.println("Create Pdf Image: " + width + "x" + height);
	}
	
	/**
	 * Destroy the back-buffer.
	 * Synchronized to drawBuffer.
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
	
	/**
	 * Renders the active pdf page
	 */
	private void renderPdf(final PDPage page, final int width, final int height, final float renderFactorX, final float renderFactorY) {
		final Composite defaultComp = _graphics.getComposite();
		final AffineTransform defaultTransform = new AffineTransform();
		_graphics.setTransform(defaultTransform);
		_graphics.setComposite(AlphaComposite.Clear); 
		_graphics.setColor(new Color(0, 0, 0, 0));
		_graphics.fillRect(0, 0, width, height);
		_graphics.setComposite(defaultComp);
		if (page != null) {
			final AffineTransform transform = new AffineTransform();
			transform.translate(0, renderFactorY);
			transform.scale(renderFactorX/page.getMediaBox().getWidth(), -renderFactorY/page.getMediaBox().getHeight());
			_graphics.setTransform(transform);
			System.out.println("Init Render Pdf");
			try {
				IGraphicsContext graphicsCtx = new CwtAwtGraphicsContext(_graphics);
				CSContent content = page.getContentStream();
				if (content != null) {
					System.out.println("Render Pdf...");
					CSPlatformRenderer renderer = new CSPlatformRenderer(null, graphicsCtx);
					renderer.process(content, page.getResources());
				}
				System.out.println("Rendered Pdf");
			} catch (Exception e) {
				System.out.flush();
				e.printStackTrace();
				System.err.flush();
				System.out.println("Can't render Pdf");
			}
		} else {
			System.out.println("Don't Render Pdf");
		}
	}
}
