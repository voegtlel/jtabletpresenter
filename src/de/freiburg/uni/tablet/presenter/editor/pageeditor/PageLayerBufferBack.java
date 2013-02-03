package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

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
	protected void paint(final IRenderable renderable) {
		renderable.render(this);
	}
	
	@Override
	protected void repaint() {
		clear();
		if (_repaintListener != null) {
			_repaintListener.render(this);
		}
	}

	@Override
	public void setRepaintListener(final IPageRepaintListener repaintListener) {
		_repaintListener = repaintListener;
		requireRepaint();
	}
}
