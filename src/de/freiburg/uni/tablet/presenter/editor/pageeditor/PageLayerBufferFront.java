package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class PageLayerBufferFront extends AbstractPageLayerBuffer implements
		IPageFrontRenderer {
	public PageLayerBufferFront(final IDisplayRenderer displayRenderer) {
		super(displayRenderer);
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
		if (_graphics != null) {
			final Graphics2D g = _displayRenderer.createRenderer();
			setRenderingHints(g);
			draw(_graphics, pen, x1, y1, x2, y2);
			draw(g, pen, x1, y1, x2, y2);
			_displayRenderer.updateRenderer(g);
		}
	}

	@Override
	public void draw(final IPen pen, final float x, final float y) {
		if (_graphics != null) {
			final Graphics2D g = _displayRenderer.createRenderer();
			setRenderingHints(g);
			draw(_graphics, pen, x, y);
			draw(g, pen, x, y);
			_displayRenderer.updateRenderer(g);
		}
	}
}
