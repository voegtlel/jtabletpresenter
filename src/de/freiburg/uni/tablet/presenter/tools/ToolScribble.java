package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.geometry.Scribble;
import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolScribble extends AbstractTool {
	private final IPen _pen = new SolidPen();
	private Scribble _scribble = null;
	private DataPoint _lastData = null;
	private final IPageFrontRenderer _renderer;

	/**
	 * 
	 * @param container
	 *            used for cursor changing
	 */
	public ToolScribble(final IToolContainer container,
			final IPageFrontRenderer renderer) {
		super(container);
		_renderer = renderer;
		updateCursor();
	}

	@Override
	public void begin() {
		_scribble = new Scribble(_pen);
	}

	@Override
	public void draw(final DataPoint data) {
		if (_scribble != null) {
			if (_lastData != null) {
				_renderer.drawFront(_pen, _lastData.getX(), _lastData.getY(),
						data.getX(), data.getY());
			} else {
				_renderer.drawFront(_pen, data.getX(), data.getY());
			}
			_scribble.addPoint(data);
			_lastData = data;
		}
	}

	@Override
	public IRenderable end() {
		final IRenderable result = _scribble;
		_scribble = null;
		_lastData = null;
		return result;
	}

	@Override
	protected Cursor generateCursor() {
		if (_pen == null) {
			return null;
		}
		final int diameter = (int) Math.max(_pen.getThickness(), 2);
		final int extraline = 3;
		final BufferedImage img = new BufferedImage(diameter + (2 * extraline)
				+ 1, diameter + (2 * extraline) + 1,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.BLACK);
		g.drawLine(0, (diameter / 2) + extraline, diameter + (extraline * 2),
				(diameter / 2) + extraline);
		g.drawLine((diameter / 2) + extraline, 0, (diameter / 2) + extraline,
				diameter + (extraline * 2));
		g.setPaint(_pen.getPaint());
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
