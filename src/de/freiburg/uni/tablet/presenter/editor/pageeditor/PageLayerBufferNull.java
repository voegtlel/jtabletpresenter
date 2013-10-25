package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public class PageLayerBufferNull implements IPageLayerBuffer {
	@Override
	public void resize(final int width, final int height, final int offsetX, final int offsetY) {
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
	}

	@Override
	public Float getDesiredRatio() {
		return null;
	}
}
