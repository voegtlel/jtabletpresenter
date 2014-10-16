package de.freiburg.uni.tablet.presenter.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public class SolidPen implements IPen {
	private static final float DEFAULT_PRESSURE = 0.5f;
	private final BasicStroke _stroke;
	private final Color _paint;
	private final float _thickness;

	public SolidPen() {
		this(1.0f, Color.BLACK);
	}

	public SolidPen(final float thickness, final Color color) {
		_thickness = thickness;
		_stroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		_paint = color;
	}

	public SolidPen(final BinaryDeserializer reader) throws IOException {
		_thickness = reader.readFloat();
		final int color = reader.readInt();
		_paint = new Color((color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff,
				(color >> 24) & 0xff);
		_stroke = new BasicStroke(_thickness, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
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
	public float getThickness(final float pressure) {
		return _thickness * (pressure + DEFAULT_PRESSURE);
	}

	@Override
	public Stroke getStroke() {
		return _stroke;
	}
	
	@Override
	public Stroke getStroke(final float pressure) {
		return new BasicStroke(_thickness * (pressure + DEFAULT_PRESSURE), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
	}

	@Override
	public Color getColor() {
		return _paint;
	}
}
