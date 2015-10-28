package de.freiburg.uni.tablet.presenter.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;

public class SolidPen implements IPen {
	private float _strokeWidth;
	private BasicStroke _stroke;
	private final Color _paint;
	private final float _thickness;

	public SolidPen() {
		this(1.0f, Color.BLACK);
	}

	public SolidPen(final float thickness, final Color color) {
		_thickness = thickness;
		_paint = color;
	}

	public SolidPen(final BinaryDeserializer reader) throws IOException {
		_thickness = reader.readFloat();
		final int color = reader.readInt();
		_paint = new Color((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff,
				(color >> 24) & 0xff);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeFloat(getThickness());
		writer.writeInt((getColor().getAlpha() << 24) | getColor().getRGB());
	}

	@Override
	public float getThickness() {
		return _thickness;
	}
	
	@Override
	public float getThickness(final RenderMetric metric, final float pressure) {
		return metric.strokeSize * _thickness * pressure;
	}

	@Override
	public Stroke getStroke(final RenderMetric metric) {
		float requestedStrokeWidth = getThickness(metric, DEFAULT_PRESSURE);
		if (_stroke == null || Math.abs(_strokeWidth - requestedStrokeWidth) > 0.1f) {
			_strokeWidth = requestedStrokeWidth;
			_stroke = new BasicStroke(_strokeWidth, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
		}
		return _stroke;
	}
	
	@Override
	public Stroke getStroke(final RenderMetric metric, final float pressure) {
		return new BasicStroke(getThickness(metric, pressure), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
	}

	@Override
	public Color getColor() {
		return _paint;
	}
}
