package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public class PageLayerBufferColor implements IPageLayerBuffer {
	private int _width = 0;
	private int _height = 0;
	private Color _color = Color.WHITE;
	
	@Override
	public void resize(final int width, final int height) {
		_width = width;
		_height = height;
	}

	public void setColor(final Color color) {
		_color = color;
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		g.setBackground(_color);
		g.clearRect(0, 0, _width, _height);
	}
}
