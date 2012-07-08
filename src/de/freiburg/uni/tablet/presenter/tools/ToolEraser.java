package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolEraser extends AbstractTool {
	private final IPen _pen = new SolidPen(15.0f, new Color(0xFF, 0xFF, 0xFF,
			0xAA));

	private EraseInfo _eraseInfo = null;

	private final boolean _checkOnlyBoundaries = false;

	/**
	 * 
	 * @param container
	 *            used for cursor changing
	 */
	public ToolEraser(final IToolPageEditor editor) {
		super(editor);
		updateCursor();
	}

	@Override
	public void begin() {
		_eraseInfo = new EraseInfo(_editor.getDocumentEditor().getDocument(),
				_editor.getDocumentEditor().getActiveLayer());
	}

	@Override
	public void draw(final DataPoint data) {
		final DocumentPageLayer activeLayer = _editor.getDocumentEditor()
				.getActiveLayer();
		if (activeLayer != null) {
			_eraseInfo.createCollisionInfo(data.getX(), data.getY(),
					data.getXOrig(), data.getYOrig(),
					data.getX() / data.getXOrig() * _pen.getThickness() / 2.0f,
					data.getY() / data.getYOrig() * _pen.getThickness() / 2.0f,
					_pen.getThickness(), _pen.getThickness(),
					_checkOnlyBoundaries);
			activeLayer.eraseAt(_eraseInfo);
			_editor.getFrontRenderer().draw(_pen, data.getX(), data.getY());
		}
	}

	@Override
	public void end() {
		_eraseInfo.applyModifications();
		_eraseInfo = null;

		_editor.getFrontRenderer().clear();
		_editor.getPageEditor().clear();
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
				.createCustomCursor(img, new Point(diameter / 2, diameter / 2),
						"EraserCursor");
		return newCursor;
	}
}
