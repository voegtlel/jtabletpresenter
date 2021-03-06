package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.BitmapImageData;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.PptxSlideSerializable;

public class PageLayerBufferBackground implements IPageLayerBufferBackground {
	private final IDisplayRenderer _displayRenderer;
	
	private Object _repaintSync = new Object();

	private boolean _ratioEnabled = false;

	private final IPageLayerBufferBackground _pdfLayer;
	private final IPageLayerBufferBackground _pptxLayer;

	private IEntity _backgroundEntity;
	
	/**
	 * Create an empty pdf renderer
	 * @param displayRenderer
	 */
	public PageLayerBufferBackground(final IDisplayRenderer displayRenderer, final int pdfLibraryType) {
		_displayRenderer = displayRenderer;
		
		_pptxLayer = new PageLayerBufferPptx(_displayRenderer);
		
		if (pdfLibraryType == PageLayerBufferComposite.PDF_LIBRARY_TYPE_JPOD) {
			_pdfLayer = new PageLayerBufferPdf(
					_displayRenderer);
		} else if (pdfLibraryType == PageLayerBufferComposite.PDF_LIBRARY_TYPE_MUPDF) {
			_pdfLayer = new PageLayerBufferPdf2(
					_displayRenderer);
		} else {
			throw new IllegalArgumentException("libraryType for pdf layer invalid");
		}
	}
	
	@Override
	public void resize(final RenderMetric renderMetric) {
		// Always resize PDF layer
		_pdfLayer.resize(renderMetric);
		_pptxLayer.resize(renderMetric);
	}
	
	@Override
	public void setBackgroundEntity(final IEntity backgroundEntity) {
		synchronized (_repaintSync) {
			_backgroundEntity = backgroundEntity;
			if (backgroundEntity instanceof PdfPageSerializable) {
				_pdfLayer.setBackgroundEntity(backgroundEntity);
			} else {
				_pdfLayer.setBackgroundEntity(null);
			}
			if (backgroundEntity instanceof PptxSlideSerializable) {
				_pptxLayer.setBackgroundEntity(backgroundEntity);
			} else {
				_pptxLayer.setBackgroundEntity(null);
			}
			_displayRenderer.requireRepaint();
		}
	}
	
	@Override
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
		IEntity backgroundEntity;
		synchronized (_repaintSync) {
			backgroundEntity = _backgroundEntity;
		}
		
		if (backgroundEntity == null) {
			return;
		} else if (backgroundEntity instanceof PdfPageSerializable) {
			_pdfLayer.drawBuffer(g, renderMetric);
		} else if (backgroundEntity instanceof PptxSlideSerializable) {
			_pptxLayer.drawBuffer(g, renderMetric);
		} else if (backgroundEntity instanceof BitmapImageData) {
			g.drawImage(((BitmapImageData) backgroundEntity).getImage(), renderMetric.surfaceDrawOffsetX, renderMetric.surfaceDrawOffsetY, renderMetric.surfaceWidth, renderMetric.surfaceHeight, null);
		}
	}
	
	@Override
	public void setRatioEnabled(final boolean enabled) {
		_ratioEnabled = enabled;
		_pdfLayer.setRatioEnabled(enabled);
		_pptxLayer.setRatioEnabled(enabled);
	}
	
	@Override
	public boolean isRatioEnabled() {
		return _ratioEnabled;
	}

	@Override
	public Float getDesiredRatio() {
		synchronized (_repaintSync) {
			if (_ratioEnabled && _backgroundEntity != null) {
				if (_backgroundEntity instanceof PdfPageSerializable) {
					return _pdfLayer.getDesiredRatio();
				} else if (_backgroundEntity instanceof PptxSlideSerializable) {
					return _pptxLayer.getDesiredRatio();
				} else if (_backgroundEntity instanceof BitmapImageData) {
					BufferedImage image = ((BitmapImageData) _backgroundEntity).getImage();
					return (float)image.getWidth() / image.getHeight();
				}
			}
		}
		return null;
	}
}
