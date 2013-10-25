package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public class PageLayerBufferColor implements IPageLayerBuffer {
	private int _width = 0;
	private int _height = 0;
	private int _offsetX = 0;
	private int _offsetY = 0;
	private Color _color = Color.WHITE;
	private Float _desiredRatio;
	
	@Override
	public void resize(final int width, final int height, final int offsetX, final int offsetY) {
		synchronized (this) {
			_width = width;
			_height = height;
			_offsetX = offsetX;
			_offsetY = offsetY;
		}
	}

	public void setColor(final Color color) {
		_color = color;
	}

	@Override
	public void drawBuffer(final Graphics2D g, final ImageObserver obs) {
		final int width;
		final int height;
		final int offsetX;
		final int offsetY;
		synchronized (this) {
			width = _width;
			height = _height;
			offsetX = _offsetX;
			offsetY = _offsetY;
		}
		//g.setBackground(_color);
		//g.clearRect(0, 0, _width, _height);
		g.setColor(_color);
		g.fillRect(offsetX, offsetY, width, height);
	}

	@Override
	public Float getDesiredRatio() {
		return _desiredRatio;
	}

	public void setDesiredRatio(final Float desiredRatio) {
		_desiredRatio = desiredRatio;
	}
}
