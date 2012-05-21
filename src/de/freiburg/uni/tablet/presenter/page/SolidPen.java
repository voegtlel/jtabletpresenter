package de.freiburg.uni.tablet.presenter.page;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

public class SolidPen implements IPen {
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

	@Override
	public float getThickness() {
		return _thickness;
	}

	public Color getColor() {
		return _paint;
	}

	@Override
	public Stroke getStroke() {
		return _stroke;
	}

	@Override
	public Paint getPaint() {
		return _paint;
	}

}
