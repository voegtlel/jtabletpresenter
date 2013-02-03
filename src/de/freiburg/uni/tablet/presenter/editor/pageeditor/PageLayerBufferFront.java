package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.graphics.Paint;

public class PageLayerBufferFront extends PageLayerBufferBack {
	public PageLayerBufferFront(final IDisplayRenderer displayRenderer) {
		super(displayRenderer);
	}
	
	@Override
	protected void setRenderingHints(final Paint g) {
		g.setAntiAlias(false);
		g.setDither(false);
	}
}
