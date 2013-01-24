package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PageLayerBufferFront extends AbstractPageLayerBuffer implements
		IPageFrontRenderer {
	private Object _renderSync = new Object();
	private boolean _requireClear = true;
	
	public PageLayerBufferFront(final IDisplayRenderer displayRenderer) {
		super(displayRenderer);
	}
	
	@Override
	public void requireClear() {
		_requireClear = true;
		requireRepaint();
	}
	
	@Override
	protected void repaint() {
		if (_requireClear) {
			System.out.println("Clear front");
			_requireClear = false;
			clear(_graphics);
		}
	}
	
	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		synchronized (_renderSync) {
			System.out.println("Draw front");
			super.drawBuffer(g, obs);
		}
	}

	@Override
	protected void setRenderingHints(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	@Override
	public void draw(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		synchronized (_renderSync) {
			if (_graphics != null) {
				draw(_graphics, pen, x1, y1, x2, y2);
				requireRepaint();
			}
		}
	}

	@Override
	public void draw(final IPen pen, final float x, final float y) {
		synchronized (_renderSync) {
			if (_graphics != null) {
				draw(_graphics, pen, x, y);
				requireRepaint();
			}
		}
	}
	
	@Override
	public void draw(final BufferedImage image, final float x, final float y, final float width, final float height) {
		synchronized (_renderSync) {
			if (_graphics != null) {
				draw(_graphics, image, x, y, width, height, _displayRenderer.getObserver());
				requireRepaint();
			}
		}
	}
	
	@Override
	public void clear() {
		synchronized (_renderSync) {
			if (_graphics != null) {
				System.out.println("Clear front immediately");
				_requireClear = false;
				clear(_graphics);
				requireRepaint();
			}
		}
	}
}
