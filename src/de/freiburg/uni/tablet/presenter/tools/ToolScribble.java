package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.AbstractRenderable;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.Scribble;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;

public class ToolScribble extends AbstractTool {
	private Scribble _scribble = null;
	private boolean _dragLines;
	private boolean _isFirstPoint = false;
	
	/**
	 * 
	 * @param editor
	 *            used for cursor changing
	 * @param dragLines if true, this scribble tool drags lines instead of scribbling
	 */
	public ToolScribble(final IToolPageEditor editor, final boolean dragLines) {
		super(editor);
		_dragLines = dragLines;
		editor.getDocumentEditor().addListener(new DocumentEditorAdapter() {
			@Override
			public void currentPenChanged(final IPen lastPen) {
				updateCursor();
			}
		});
		updateCursor();
	}

	@Override
	synchronized public void begin() {
		_scribble = new Scribble(_editor.getDocumentEditor().getCurrentPage(), _editor.getDocumentEditor().getCurrentPen());
		_editor.getFrontRenderer().setRepaintListener(this);
		_isFirstPoint = true;
	}
	
	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		if (_scribble != null) {
			_scribble.render(renderer);
		}
	}

	@Override
	synchronized public void draw(final DataPoint data) {
		if (_scribble != null) {
			if (_dragLines) {
				if (_isFirstPoint ) {
					_isFirstPoint = false;
					_scribble.addPoint(data);
					_scribble.addPoint(data.clone());
				} else {
					_scribble.updateLastPoint(data);
				}
			} else {
				_scribble.addPoint(data);
			}
			_editor.getFrontRenderer().requireRepaint(_scribble, true);
		}
	}

	@Override
	synchronized public void end() {
		final AbstractRenderable result = _scribble;
		_scribble = null;

		_editor.getPageEditor().suspendRepaint();
		if (result != null) {
			_editor.getDocumentEditor().getCurrentPage().addRenderable(result);
		}
		
		_editor.getFrontRenderer().setRepaintListener(null);
		
		_editor.getPageEditor().resumeRepaint();
	}

	@Override
	protected Cursor generateCursor() {
		if (_editor.getDocumentEditor().getCurrentPen() == null) {
			return null;
		}
		final int diameter = (int) Math.max(_editor.getDocumentEditor()
				.getCurrentPen().getThickness(), 2);
		final int extraline = 3;
		final BufferedImage img = createBitmap(diameter + 2 * extraline + 1,
				diameter + 2 * extraline + 1);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.BLACK);
		g.drawLine(0, diameter / 2 + extraline, diameter + extraline * 2,
				diameter / 2 + extraline);
		g.drawLine(diameter / 2 + extraline, 0, diameter / 2 + extraline,
				diameter + extraline * 2);
		g.setPaint(_editor.getDocumentEditor().getCurrentPen().getColor());
		g.fillOval(extraline, extraline, diameter, diameter);
		img.flush();
		g.dispose();
		final Cursor newCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(
						img,
						new Point(diameter / 2 + extraline, diameter / 2
								+ extraline), "ScribbleCursor");
		return newCursor;
	}
}
