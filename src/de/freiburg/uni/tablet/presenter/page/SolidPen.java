package de.freiburg.uni.tablet.presenter.page;

import java.io.IOException;

import android.graphics.Color;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public class SolidPen implements IPen {
	private final int _paint;
	private final Cap _strokeCap;
	private final Join _strokeJoin;
	private final float _strokeMiter;
	private final float _strokeWidth;

	public SolidPen() {
		this(1.0f, Color.BLACK);
	}

	public SolidPen(final float thickness, final int color) {
		_strokeCap = Cap.ROUND;
		_strokeJoin = Join.ROUND;
		_strokeWidth = thickness;
		_strokeMiter = 0;
		_paint = color;
	}

	public SolidPen(final BinaryDeserializer reader) throws IOException {
		_strokeWidth = reader.readFloat();
		_paint = reader.readInt();
		_strokeCap = Cap.ROUND;
		_strokeJoin = Join.ROUND;
		_strokeMiter = 0;
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeFloat(getThickness());
		writer.writeInt(getColor());
	}

	@Override
	public float getThickness() {
		return _strokeWidth;
	}

	@Override
	public Cap getStrokeCap() {
		return _strokeCap;
	}
	
	@Override
	public Join getStrokeJoin() {
		return _strokeJoin;
	}
	
	@Override
	public float getStrokeMiter() {
		return _strokeMiter;
	}
	
	@Override
	public float getStrokeWidth() {
		return _strokeWidth;
	}

	@Override
	public int getColor() {
		return _paint;
	}
}
