package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Graphics2D;

public class PageLayerBufferNull implements IPageLayerBuffer {
	@Override
	public void resize(final int width, final int height) {
	}

	@Override
	public void drawBuffer(final Graphics2D g) {
	}

	@Override
	public void clear() {
	}
}
