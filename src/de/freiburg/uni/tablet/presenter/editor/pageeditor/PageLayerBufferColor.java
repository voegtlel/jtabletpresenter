package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Color;
import java.awt.Graphics2D;

public class PageLayerBufferColor implements IPageLayerBuffer {
	private Color _color = Color.WHITE;
	private Float _desiredRatio;
	
	@Override
	public void resize(final RenderMetric renderMetric) {
	}

	public void setColor(final Color color) {
		_color = color;
	}

	@Override
	public void drawBuffer(final Graphics2D g, final RenderMetric renderMetric) {
		g.setColor(_color);
		g.fillRect(renderMetric.surfaceDrawOffsetX, renderMetric.surfaceDrawOffsetY, renderMetric.surfaceWidth, renderMetric.surfaceHeight);
	}

	@Override
	public Float getDesiredRatio() {
		return _desiredRatio;
	}

	public void setDesiredRatio(final Float desiredRatio) {
		_desiredRatio = desiredRatio;
	}
}
