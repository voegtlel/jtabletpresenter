package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.geometry.Scribble;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolScribble extends AbstractTool {
	public IPen _pen = new SolidPen();
	public Scribble _scribble = null;

	public ToolScribble(final Container container) {
		super(container);
	}

	@Override
	public void Begin(final DataPoint data) {
		_scribble = new Scribble(_pen);
	}

	@Override
	public void Draw(final DataPoint data) {
		if (_scribble != null) {
			_scribble.addPoint(data);
		}
	}

	@Override
	public IRenderable End(final DataPoint data) {
		final IRenderable result = _scribble;
		_scribble = null;
		return result;
	}

	@Override
	protected Cursor generateCursor() {
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
