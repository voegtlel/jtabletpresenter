package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;

import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class PageLayerBufferPdf implements IPageLayerBufferBackground {
	private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
	
	protected Image _imageBuffer = null;

	protected int _sizeX = 1;
	protected int _sizeY = 1;
	
	private PdfPageSerializable _pdfPage;
	private IDisplayRenderer _displayRenderer;
	
	private Graphics2D _graphics;
	
	private Object _repaintSync = new Object();
	
	private boolean _requireRepaint = true;

	private boolean _ratioEnabled = false;
	
	/**
	 * Create an empty pdf renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferPdf(final IDisplayRenderer displayRenderer) {
		_displayRenderer = displayRenderer;
	}
	
	@Override
	public void resize(final RenderMetric renderMetric) {
	}
	
	/**
	 * Sets the document
	 * @param document
	 */
	@Override
	public void setBackgroundEntity(final IEntity backgroundEntity) {
		if (backgroundEntity instanceof PdfPageSerializable) {
			synchronized (_repaintSync) {
				if (backgroundEntity != _pdfPage) {
					_pdfPage = (PdfPageSerializable) backgroundEntity;
					_requireRepaint = true;
					_displayRenderer.requireRepaint();
				}
			}
		}
	}
	
	@Override
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
		boolean requiresResize;
		boolean requiresRepaint;
		PDPage page;
		synchronized (_repaintSync) {
			requiresResize = (_sizeX != renderMetric.surfaceWidth || _sizeY != renderMetric.surfaceHeight);
			_sizeX = renderMetric.surfaceWidth;
			_sizeY = renderMetric.surfaceHeight;
			requiresRepaint = _requireRepaint;
			_requireRepaint = false;
			page = ((_pdfPage == null)?null:_pdfPage.getPage());
		}
		if (page == null) {
			destroyBuffer();
		} else {
			if (_imageBuffer == null || requiresResize) {
				createImage(renderMetric.surfaceWidth, renderMetric.surfaceHeight);
				requiresRepaint = true;
			}
			if (requiresRepaint) {
				renderPdf(page, renderMetric);
			}
			g.drawImage(_imageBuffer, renderMetric.surfaceDrawOffsetX, renderMetric.surfaceDrawOffsetY, null);
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
	private void renderPdf(final PDPage page, final RenderMetric renderMetric) {
		final Composite defaultComp = _graphics.getComposite();
		_graphics.setTransform(IDENTITY_TRANSFORM);
		_graphics.setComposite(AlphaComposite.Clear);
		_graphics.setColor(new Color(0, 0, 0, 0));
		_graphics.fillRect(0, 0, renderMetric.surfaceWidth, renderMetric.surfaceHeight);
		_graphics.setComposite(defaultComp);
		if (page != null) {
			final AffineTransform transform = AffineTransform.getTranslateInstance(0, renderMetric.surfaceHeight);
			transform.scale(renderMetric.surfaceWidth/page.getMediaBox().getWidth(), -renderMetric.surfaceHeight/page.getMediaBox().getHeight());
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
	
	@Override
	public void setRatioEnabled(final boolean enabled) {
		_ratioEnabled = enabled;
	}
	
	@Override
	public boolean isRatioEnabled() {
		return _ratioEnabled;
	}

	@Override
	public Float getDesiredRatio() {
		if (_ratioEnabled && _pdfPage != null && _pdfPage.getPage() != null) {
			final CDSRectangle r = _pdfPage.getPage().getMediaBox();
			return r.getWidth() / r.getHeight();
		}
		return null;
	}
}
