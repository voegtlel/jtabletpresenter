package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawSlide;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.PptxSlideSerializable;

public class PageLayerBufferPptx implements IPageLayerBufferBackground {
	private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
	
	protected Image _imageBuffer = null;

	protected int _sizeX = 1;
	protected int _sizeY = 1;
	
	private PptxSlideSerializable _pptxSlide;
	private IDisplayRenderer _displayRenderer;
	
	private Graphics2D _graphics;
	
	private Object _repaintSync = new Object();
	
	private boolean _requireRepaint = true;

	private boolean _ratioEnabled = false;
	
	/**
	 * Create an empty renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferPptx(final IDisplayRenderer displayRenderer) {
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
		if (backgroundEntity instanceof PptxSlideSerializable) {
			synchronized (_repaintSync) {
				if (backgroundEntity != _pptxSlide) {
					_pptxSlide = (PptxSlideSerializable) backgroundEntity;
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
		XSLFSlide slide;
		XMLSlideShow pptx;
		synchronized (_repaintSync) {
			requiresResize = (_sizeX != renderMetric.surfaceWidth || _sizeY != renderMetric.surfaceHeight);
			_sizeX = renderMetric.surfaceWidth;
			_sizeY = renderMetric.surfaceHeight;
			requiresRepaint = _requireRepaint;
			_requireRepaint = false;
			slide = ((_pptxSlide == null)?null:_pptxSlide.getSlide());
			pptx = ((_pptxSlide==null)?null:_pptxSlide.getParentPptx().getDocument());
		}
		if (slide == null) {
			destroyBuffer();
		} else {
			if (_imageBuffer == null || requiresResize) {
				createImage(renderMetric.surfaceWidth, renderMetric.surfaceHeight);
				requiresRepaint = true;
			}
			if (requiresRepaint) {
				renderSlide(slide, pptx, renderMetric);
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
		System.out.println("Create Slide Image: " + width + "x" + height);
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
	 * Renders the active slide
	 * @param pptx
	 */
	private void renderSlide(final XSLFSlide slide, final XMLSlideShow pptx, final RenderMetric renderMetric) {
		final Composite defaultComp = _graphics.getComposite();
		_graphics.setTransform(IDENTITY_TRANSFORM);
		_graphics.setComposite(AlphaComposite.Clear);
		_graphics.setColor(new Color(0, 0, 0, 0));
		_graphics.fillRect(0, 0, renderMetric.surfaceWidth, renderMetric.surfaceHeight);
		_graphics.setComposite(defaultComp);
		_graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (slide != null) {
			Dimension pageSize = pptx.getPageSize();
			final AffineTransform transform = AffineTransform.getScaleInstance(
					renderMetric.surfaceWidth/pageSize.getWidth(), renderMetric.surfaceHeight/pageSize.getHeight());
			_graphics.setTransform(transform);
			System.out.println("Init Render Slide");
			try {
				DrawFactory instance = DrawFactory.getInstance(_graphics);
				DrawSlide drawable = instance.getDrawable(slide);
				drawable.draw(_graphics);
				System.out.println("Rendered Slide");
			} catch (Exception e) {
				System.out.flush();
				e.printStackTrace();
				System.err.flush();
				System.out.println("Can't render Slide");
			}
		} else {
			System.out.println("Don't Render Slide");
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
		if (_ratioEnabled && _pptxSlide != null && _pptxSlide.getSlide() != null) {
			final Dimension r = _pptxSlide.getParentPptx().getDocument().getPageSize();
			return (float)(r.getWidth() / r.getHeight());
		}
		return null;
	}
}
