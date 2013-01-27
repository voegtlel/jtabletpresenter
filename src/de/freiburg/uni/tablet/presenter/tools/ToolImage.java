package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

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
	synchronized public void render(final IPageBackRenderer renderer) {
		if (_image != null) {
			_image.render(renderer);
		}
	}

	@Override
	synchronized public void begin() {
		final BitmapImage currentImage = _editor.getDocumentEditor().getCurrentImage();
		if (currentImage == null) {
			JOptionPane.showMessageDialog(_editor.getPageEditor().getContainerComponent(),
					"No image selected for image tool");
		} else {
			_image = currentImage.cloneRenderable(_editor.getDocumentEditor().getCurrentPage());
		}
		_editor.getFrontRenderer().setRepaintListener(this);
	}

	@Override
	synchronized public void draw(final DataPoint data) {
		if (_image != null) {
			if (_startData == null) {
				_image.setLocation(data.getX(), data.getY());
				_startData = data;
			} else {
				final float x1 = Math.min(data.getX(), _startData.getX());
				final float y1 = Math.min(data.getY(), _startData.getY());
				final float x2 = Math.max(data.getX(), _startData.getX());
				final float y2 = Math.max(data.getY(), _startData.getY());
				_image.setLocation(x1, y1);
				_image.setSize(x2-x1, y2-y1);
				_editor.getFrontRenderer().requireRepaint();
			}
		}
	}

	@Override
	synchronized public void end() {
		final BitmapImage result = _image;
		_image = null;
		_startData = null;

		_editor.getPageEditor().suspendRepaint();
		if (result != null && (((result.getMaxX() - result.getMinX()) > 0)
				|| ((result.getMaxY() - result.getMinY()) > 0))) {
			_editor.getDocumentEditor().getCurrentPage().addRenderable(result);
		}
		
		_editor.getFrontRenderer().setRepaintListener(null);
		
		_editor.getFrontRenderer().requireRepaint();
		_editor.getPageEditor().resumeRepaint();
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
