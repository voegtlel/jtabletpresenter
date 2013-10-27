package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.Text;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class ToolText extends AbstractTool {
	private TextFont _font = null;
	private DataPoint _startData = null;
	
	/**
	 * Documents
	 * @param container
	 *            used for cursor changing
	 */
	public ToolText(final IToolPageEditor editor) {
		super(editor);
		updateCursor();
	}
	
	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
	}

	@Override
	synchronized public void begin() {
		_editor.getFrontRenderer().setRepaintListener(this);
		_font = new TextFont(_editor.getDocumentEditor().getFrontDocument(), "Font1", 0.05f);
	}

	@Override
	synchronized public void draw(final DataPoint data) {
		_startData = data;
	}

	@Override
	synchronized public void end() {
		Text newElement = new Text(_editor.getDocumentEditor().getCurrentPage(), _startData.getX(), _startData.getY(), "Blablaäöü+~'#´`<>|!°^", _font);
		_startData = null;

		_editor.getPageEditor().suspendRepaint();
		_editor.getDocumentEditor().getCurrentPage().addRenderable(newElement);
		
		_editor.getFrontRenderer().setRepaintListener(null);
		
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
