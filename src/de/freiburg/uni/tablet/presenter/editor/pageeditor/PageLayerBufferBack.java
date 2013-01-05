package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PageLayerBufferBack extends AbstractPageLayerBuffer implements
		IPageBackRenderer {
	private IPageRepaintListener _repaintListener = null;

	public PageLayerBufferBack(final IDisplayRenderer displayRenderer) {
		super(displayRenderer);
	}

	@Override
	protected void setRenderingHints(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	@Override
	public void resize(final int width, final int height) {
		super.resize(width, height);
		if (_repaintListener != null) {
			_repaintListener.render(this);
		}
	}

	@Override
	public void draw(final IPen pen, final Path2D path) {
		if (_graphics != null) {
			draw(_graphics, pen, path);
		}
	}

	@Override
	public void draw(final IPen pen, final float x, final float y) {
		if (_graphics != null) {
			draw(_graphics, pen, x, y);
		}
	}

	@Override
	public void drawGridLine(final IPen pen, final float x1, final float y1,
			final float x2, final float y2) {
		if (_graphics != null) {
			draw(_graphics, pen, x1, y1, x2, y2);
		}
	}
	
	@Override
	public void draw(BufferedImage image, float x, float y, float width, float height) {
		if (_graphics != null) {
			draw(_graphics, image,  x, y, width, height, _displayRenderer.getObserver());
		}
	}

	@Override
	public void setRepaintListener(final IPageRepaintListener repaintListener) {
		_repaintListener = repaintListener;
	}
}
