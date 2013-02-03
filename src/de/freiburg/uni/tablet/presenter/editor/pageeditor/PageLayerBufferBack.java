package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.graphics.Paint;
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
	protected void setRenderingHints(final Paint g) {
		g.setAntiAlias(true);
		g.setDither(true);
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
