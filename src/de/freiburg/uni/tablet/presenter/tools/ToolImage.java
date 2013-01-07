package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.AbstractRenderable;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;

public class ToolImage extends AbstractTool {
	private BitmapImage _image = null;
	private DataPoint _startData = null;

	/**
	 * Documents
	 * @param container
	 *            used for cursor changing
	 */
	public ToolImage(final IToolPageEditor editor) {
		super(editor);
		updateCursor();
	}

	@Override
	public void begin() {
		_image = (BitmapImage) _editor.getDocumentEditor().getCurrentImage().cloneRenderable(_editor.getDocumentEditor().getDocument()
				.getNextId());
	}

	@Override
	public void draw(final DataPoint data) {
		if (_image != null) {
			if (_startData == null) {
				_image.setLocation(data.getX(), data.getY());
				_startData = data;
			} else {
				float x1 = Math.min(data.getX(), _startData.getX());
				float y1 = Math.min(data.getY(), _startData.getY());
				float x2 = Math.max(data.getX(), _startData.getX());
				float y2 = Math.max(data.getY(), _startData.getY());
				_image.setLocation(x1, y1);
				_image.setSize(x2-x1, y2-y1);
				_editor.getFrontRenderer().clear();
				_editor.getFrontRenderer().draw(
						_image.getImage(),
						x1, y1, x2-x1,
						y2-y1);
			}
		}
	}

	@Override
	public void end() {
		final BitmapImage result = _image;
		_image = null;
		_startData = null;

		if (((result.getMaxX() - result.getMinX()) > 0)
				|| ((result.getMaxY() - result.getMinY()) > 0)) {
			_editor.getDocumentEditor().getActiveLayer().addRenderable(result);
		}
		
		_editor.getFrontRenderer().clear();
		_editor.getPageEditor().clear(result);
	}

	@Override
	protected Cursor generateCursor() {
		if (_editor.getDocumentEditor().getCurrentPen() == null) {
			return null;
		}
		final int extraline = 3;
		final BufferedImage img = createBitmap(2 * extraline + 1,
				2 * extraline + 1);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.BLACK);
		g.drawLine(0, extraline, extraline * 2, extraline);
		g.drawLine(extraline, 0, extraline, extraline * 2);
		img.flush();
		g.dispose();
		final Cursor newCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(
						img,
						new Point(extraline, extraline), "ImageCursor");
		return newCursor;
	}
}
