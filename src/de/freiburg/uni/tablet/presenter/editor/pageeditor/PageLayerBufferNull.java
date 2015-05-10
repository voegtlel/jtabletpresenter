package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;

public class PageLayerBufferNull implements IPageLayerBuffer {
	@Override
	public void resize(final RenderMetric renderMetric) {
	}

	@Override
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
	}
	
	@Override
	public Float getDesiredRatio() {
		return null;
	}
}
