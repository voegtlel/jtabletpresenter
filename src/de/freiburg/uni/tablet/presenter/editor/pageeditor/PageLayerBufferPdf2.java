package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;

public class PageLayerBufferPdf2 implements IPageLayerBufferPdf {
	protected int _width = 1;
	protected int _height = 1;
	
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
	public void resize(final RenderMetric renderMetric) {
		synchronized (_repaintSync) {
			_requireRepaint = true;
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
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
		boolean requiresRepaint;
		Page page;
		synchronized (_repaintSync) {
			requiresRepaint = _requireRepaint;
			_width = renderMetric.surfaceWidth;
			_height = renderMetric.surfaceHeight;
			_requireRepaint = false;
			page = ((_pdfPage == null)?null:_pdfPage.getPage2());
		}
		if (page == null) {
			destroyBuffer();
		} else {
			if (requiresRepaint) {
				renderPdf(page, renderMetric);
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(_imageBuffer, renderMetric.surfaceDrawOffsetX, renderMetric.surfaceDrawOffsetY, renderMetric.surfaceDrawOffsetX + renderMetric.surfaceWidth, renderMetric.surfaceDrawOffsetY + renderMetric.surfaceHeight, _imageDataSize.x, _imageDataSize.y, _imageDataSize.width + _imageDataSize.x, _imageDataSize.height + _imageDataSize.y, null);
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
	
	private void renderPdf(final Page page, final RenderMetric renderMetric) {
		createImage(page, renderMetric.surfaceWidth, renderMetric.surfaceHeight);
		if (_imageBuffer != null) {
			System.out.println("drawPage");
			PagePixels pagePixels = page.getPagePixels();
			pagePixels.getOptions().setAntiAlias(8);
			pagePixels.getOptions().setImageType(ImageType.IMAGE_TYPE_ARGB);
			float scale = Math.min(renderMetric.surfaceVirtualWidth / page.getWidth(), renderMetric.surfaceVirtualHeight / page.getHeight());
			pagePixels.getOptions().setZoom(scale);
			Rectangle2D.Float bounds = new Rectangle2D.Float(
					renderMetric.surfaceRelativeOffsetX * page.getWidth() / renderMetric.surfaceVirtualWidth,
					renderMetric.surfaceRelativeOffsetY * page.getHeight() / renderMetric.surfaceVirtualHeight,
					renderMetric.surfaceWidth * page.getWidth() / renderMetric.surfaceVirtualWidth,
					renderMetric.surfaceHeight * page.getHeight() / renderMetric.surfaceVirtualHeight);
			System.out.println("SrcRect: " + bounds + " of " + page.getBoundBox());
			pagePixels.drawPage(null, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
			/*final ByteBuffer buffer = page.getDocument().getPageByteBuffer(page.getPageNumber(),
					Math.min((float)_imageBuffer.getWidth() / (float)page.getWidth(), (float)_imageBuffer.getHeight() / (float)page.getHeight()),
					0, ImageType.IMAGE_TYPE_ARGB, 1.0f, bbox, 0, 0,
					page.getWidth(), page.getHeight());*/
			PageRect bbox = pagePixels.getOptions().getBoundBox();
			
			_imageDataSize.x = bbox.getX();
	    	_imageDataSize.y = bbox.getY();
	    	_imageDataSize.width = bbox.getWidth();
	    	_imageDataSize.height = bbox.getHeight();
	    	
			if (_imageDataSize.width > _imageBuffer.getWidth() || _imageDataSize.height > _imageBuffer.getHeight()) {
				_additionalRenderWidth += Math.max(_imageDataSize.width - _imageBuffer.getWidth(), 0);
				_additionalRenderHeight += Math.max(_imageDataSize.height - _imageBuffer.getHeight(), 0);
				System.out.println("Width or height too small: (rw)" + _imageDataSize.width + " > (bw)" + _imageBuffer.getWidth() + " || (rh)" +_imageDataSize.height + " > (bh)" + _imageBuffer.getHeight());
				System.out.println(" => Additional Size " + _additionalRenderWidth + "x" + _additionalRenderHeight);
				renderPdf(page, renderMetric);
			} else {
				final WritableRaster raster = _imageBuffer.getRaster();
				System.out.println("drawPage result: " + bbox.getX() + ", " + bbox.getY() + ", " + bbox.getWidth() + ", " + bbox.getHeight());
		    	//raster.setDataElements(bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight(), pagePixels.getPixels());
				raster.setDataElements(0, 0, bbox.getWidth(), bbox.getHeight(), pagePixels.getPixels());
			}
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
