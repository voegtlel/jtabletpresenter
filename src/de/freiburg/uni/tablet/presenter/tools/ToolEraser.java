package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolEraser extends AbstractTool {
	private final IPen _pen = new SolidPen(15.0f, new Color(0xFF, 0xFF, 0xFF,
			0xAA));
	private final IPageRenderer _renderer;
	private final IToolPageEditor _editor;

	/**
	 * 
	 * @param container
	 *            used for cursor changing
	 */
	public ToolEraser(final IToolContainer container,
			final IPageRenderer renderer, final IToolPageEditor editor) {
		super(container);
		_renderer = renderer;
		_editor = editor;
		updateCursor();
	}

	@Override
	public void begin() {
	}

	@Override
	public void draw(final DataPoint data) {
		final IPage page = _editor.getPage();
		if (page != null) {
			final EraseInfo eraseInfo = new EraseInfo(
					data.getX(),
					data.getY(),
					data.getXOrig(),
					data.getYOrig(),
					((data.getX() / data.getXOrig()) * _pen.getThickness()) / 2.0f,
					((data.getY() / data.getYOrig()) * _pen.getThickness()) / 2.0f,
					_pen.getThickness(), _pen.getThickness());
			page.eraseAt(eraseInfo);
			_renderer.drawFront(_pen, data.getX(), data.getY());
		}
	}

	@Override
	public void end() {
		_renderer.redrawBack();
		_renderer.clearFront();
	}

	@Override
	protected Cursor generateCursor() {
		if (_pen == null) {
			return null;
		}

		final int diameter = (int) Math.max(_pen.getThickness(), 2);
		final BufferedImage img = createBitmap(diameter + 1, diameter + 1);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillOval(0, 0, diameter, diameter);
		g.setColor(Color.BLACK);
		g.drawOval(0, 0, diameter, diameter);
		img.flush();
		g.dispose();
		final Cursor newCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(img,
						new Point((diameter / 2), (diameter / 2)),
						"EraserCursor");
		return newCursor;
	}
}
