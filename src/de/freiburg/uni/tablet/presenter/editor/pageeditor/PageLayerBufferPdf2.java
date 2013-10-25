package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jmupdf.enums.ImageType;
import com.jmupdf.page.Page;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;

public class PageLayerBufferPdf2 implements IPageLayerBufferPdf {
	protected int _width = 1;
	protected int _height = 1;
	protected int _offsetX;
	protected int _offsetY;
	protected float _sizeX = 1;
	protected float _sizeY = 1;
	
	protected int _additionalRenderWidth = 0;
	protected int _additionalRenderHeight = 0;

	private PdfPageSerializable _pdfPage;
	private IDisplayRenderer _displayRenderer;

	private Object _repaintSync = new Object();
	
	private boolean _requireRepaint = true;
	
	private boolean _ratioEnabled;
	
	private BufferedImage _imageBuffer;
	private Rectangle _imageDataSize = new Rectangle();
	
	/**
	 * Create an empty pdf renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferPdf2(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}
	
	@Override
	public void resize(final int width, final int height, final int offsetX, final int offsetY) {
		synchronized (_repaintSync) {
			_requireRepaint = (_width != width || _height != height);
			_width = width;
			_height = height;
			_offsetX = offsetX;
			_offsetY = offsetY;
			_sizeX = width;
			_sizeY = height;
			// Require synched?
			_additionalRenderWidth = 0;
			_additionalRenderHeight = 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBufferPdf#setPdfPage(de.freiburg.uni.tablet.presenter.document.PdfPageSerializable)
	 */
	@Override
	public void setPdfPage(final PdfPageSerializable pdfPage) {
		synchronized (_repaintSync) {
			_pdfPage = pdfPage;
			_requireRepaint = true;
			_displayRenderer.requireRepaint();
		}
	}
	
	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		int width;
		int height;
		int offsetX;
		int offsetY;
		boolean requiresRepaint;
		Page page;
		synchronized (_repaintSync) {
			width = _width;
			height = _height;
			offsetX = _offsetX;
			offsetY = _offsetY;
			requiresRepaint = _requireRepaint;
			_requireRepaint = false;
			page = ((_pdfPage == null)?null:_pdfPage.getPage2());
		}
		if (page == null) {
			destroyBuffer();
		} else {
			if (requiresRepaint) {
				renderPdf(page, width, height);
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(_imageBuffer, offsetX, offsetY, offsetX + width, offsetY + height, _imageDataSize.x, _imageDataSize.y, _imageDataSize.width + _imageDataSize.x, _imageDataSize.height + _imageDataSize.y, obs);
		}
	}
	
	/**
	 * Create the backbuffer.
	 * Synchronized to drawBuffer.
	 */
	private void createImage(final Page page, final int width, final int height) {
		float pageRatio = (float)page.getWidth() / (float)page.getHeight();
		final int imgWidth = Math.max(width, (int)Math.ceil(height * pageRatio)) + _additionalRenderWidth;
		final int imgHeight = Math.max(height, (int)Math.ceil(width / pageRatio)) + _additionalRenderHeight;
		/*_imageBuffer = _displayRenderer.createImageBuffer(imgWidth, imgHeight,
				Transparency.TRANSLUCENT);*/
		if (_imageBuffer == null || _imageBuffer.getWidth() != imgWidth || _imageBuffer.getHeight() != imgHeight)
		{
			_imageBuffer = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
			System.out.println("Create Pdf Image: " + width + "x" + height + " (real: " + imgWidth + "x" + imgHeight + ")");
		}
	}
	
	/**
	 * Destroy the back-buffer.
	 * Synchronized to drawBuffer.
	 */
	private void destroyBuffer() {
		if (_imageBuffer != null) {
			_imageBuffer = null;
		}
	}
	
	private void renderPdf(final Page page, final int width, final int height) {
		createImage(page, width, height);
		if (_imageBuffer != null) {
			System.out.println("drawPage " + width + ", " + height);
			int[] bbox = new int[4];
			page.getDocument().setAntiAliasLevel(8);
			final ByteBuffer buffer = page.getDocument().getPageByteBuffer(page.getPageNumber(), Math.min((float)_imageBuffer.getWidth() / (float)page.getWidth(), (float)_imageBuffer.getHeight() / (float)page.getHeight()), 0, ImageType.IMAGE_TYPE_ARGB, 1.0f, bbox, 0, 0, page.getWidth(), page.getHeight());
			
			_imageDataSize.x = bbox[0];
	    	_imageDataSize.y = bbox[1];
	    	_imageDataSize.width = bbox[2];
	    	_imageDataSize.height = bbox[3];
	    	
			if (_imageDataSize.width > _imageBuffer.getWidth() || _imageDataSize.height > _imageBuffer.getHeight()) {
				_additionalRenderWidth += Math.max(_imageDataSize.width - _imageBuffer.getWidth(), 0);
				_additionalRenderHeight += Math.max(_imageDataSize.height - _imageBuffer.getHeight(), 0);
				System.out.println("Width or height too small: (rw)" + _imageDataSize.width + " > (bw)" + _imageBuffer.getWidth() + " || (rh)" +_imageDataSize.height + " > (bh)" + _imageBuffer.getHeight());
				System.out.println(" => Additional Size " + _additionalRenderWidth + "x" + _additionalRenderHeight);
				renderPdf(page, width, height);
			} else {
				final int[] pixels = new int[buffer.order(ByteOrder.nativeOrder()).asIntBuffer().capacity()];
				buffer.order(ByteOrder.nativeOrder()).asIntBuffer().get(pixels);
				
		    	final WritableRaster raster = _imageBuffer.getRaster();
		    	System.out.println("drawPage result: " + bbox[0] + ", " + bbox[1] + ", " + bbox[2] + ", " + bbox[3]);
		    	raster.setDataElements(bbox[0], bbox[1], bbox[2], bbox[3], pixels);
			}
			page.getDocument().freeByteBuffer(buffer);
	    }
	}
	

	/**
	 * Enable or disable automatic page ratio (used for getDesiredRatio())
	 * @param enabled
	 */
	@Override
	public void setRatioEnabled(final boolean enabled) {
		_ratioEnabled = enabled;
	}
	
	/**
	 * Checks if the automatic ratio is enabled (used for getDesiredRatio())
	 * @return
	 */
	@Override
	public boolean isRatioEnabled() {
		return _ratioEnabled;
	}

	@Override
	public Float getDesiredRatio() {
		if (_ratioEnabled && _pdfPage != null && _pdfPage.getPage2() != null) {
			return (float)_pdfPage.getPage2().getWidth() / _pdfPage.getPage2().getHeight();
		}
		return null;
	}
}
