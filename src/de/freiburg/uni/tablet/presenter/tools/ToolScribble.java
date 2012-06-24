package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.AbstractRenderable;
import de.freiburg.uni.tablet.presenter.geometry.Scribble;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;

public class ToolScribble extends AbstractTool {
	private Scribble _scribble = null;
	private DataPoint _lastData = null;
	private final IPageRenderer _renderer;
	private final IToolPageEditor _editor;

	/**
	 * 
	 * @param container
	 *            used for cursor changing
	 */
	public ToolScribble(final IToolContainer container,
			final IPageRenderer renderer, final IToolPageEditor editor) {
		super(container);
		_renderer = renderer;
		_editor = editor;
		updateCursor();
	}

	@Override
	public void begin() {
		_scribble = new Scribble(_editor.getNextObjectId(),
				_editor.getCurrentPen());
	}

	@Override
	public void draw(final DataPoint data) {
		if (_scribble != null) {
			if (_lastData != null) {
				_renderer.drawFront(_editor.getCurrentPen(), _lastData.getX(),
						_lastData.getY(), data.getX(), data.getY());
			} else {
				_renderer.drawFront(_editor.getCurrentPen(), data.getX(),
						data.getY());
			}
			_scribble.addPoint(data);
			_lastData = data;
		}
	}

	@Override
	public void end() {
		final AbstractRenderable result = _scribble;
		_scribble = null;
		_lastData = null;
		_editor.getPage().addRenderable(result);
		result.render(_renderer);
		_renderer.clearFront();
	}

	@Override
	protected Cursor generateCursor() {
		if (_editor.getCurrentPen() == null) {
			return null;
		}
		final int diameter = (int) Math.max(_editor.getCurrentPen()
				.getThickness(), 2);
		final int extraline = 3;
		final BufferedImage img = createBitmap(diameter + (2 * extraline) + 1,
				diameter + (2 * extraline) + 1);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.BLACK);
		g.drawLine(0, (diameter / 2) + extraline, diameter + (extraline * 2),
				(diameter / 2) + extraline);
		g.drawLine((diameter / 2) + extraline, 0, (diameter / 2) + extraline,
				diameter + (extraline * 2));
		g.setPaint(_editor.getCurrentPen().getColor());
		g.fillOval(extraline, extraline, diameter, diameter);
		img.flush();
		g.dispose();
		final Cursor newCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(
						img,
						new Point((diameter / 2) + extraline, (diameter / 2)
								+ extraline), "ScribbleCursor");
		return newCursor;
	}
}
