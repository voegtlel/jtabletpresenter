package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;

public class PageLayerBufferColor implements IPageLayerBuffer {
	private int _color;
	
	public PageLayerBufferColor() {
		_color = Color.WHITE;
	}
	
	@Override
	public void resize(final int width, final int height) {
	}

	public void setColor(final int color) {
		_color = color;
	}

	@Override
	public void drawBuffer(final Canvas g) {
		g.drawColor(_color, Mode.SRC);
	}
}
