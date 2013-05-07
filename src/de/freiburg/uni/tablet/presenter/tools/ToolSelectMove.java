package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.CollisionInfo;
import de.freiburg.uni.tablet.presenter.geometry.CollisionListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolSelectMove extends AbstractTool implements CollisionListener {
	private final IPen _pen;
	private final IPen _highlightPen = new SolidPen(2.5f, Color.YELLOW);

	private DocumentPage _selectPage = null;
	
	private boolean _checkOnlyBoundaries = false;
	
	private LinkedElementList<IRenderable> _selectedObjects = new LinkedElementList<IRenderable>();
	
	private boolean _started = false;
	
	private boolean _isDragging = false;
	
	private float _dragStartX = 0f;
	private float _dragStartY = 0f;
	private float _dragEndX = 0f;
	private float _dragEndY = 0f;
	
	private float _selectStartX = 0f;
	private float _selectStartY = 0f;
	private float _selectEndX = 0f;
	private float _selectEndY = 0f;

	/**
	 * @param editor
	 * @param checkOnlyBoundaries
	 */
	public ToolSelectMove(final IToolPageEditor editor, final boolean checkOnlyBoundaries) {
		super(editor);
		_pen = new SolidPen(editor.getConfig().getFloat("editor.selectMove.thickness", 1f), new Color(0xFF, 0xFF, 0xFF,
				0xAA));
		_checkOnlyBoundaries = checkOnlyBoundaries;
		updateCursor();
	}
	
	@Override
	public void render(final IPageBackRenderer renderer) {
		if (_isDragging) {
			renderer.setOffset(_dragStartX - _dragEndX, _dragStartY - _dragEndY);
		}
		renderer.draw(_highlightPen, _selectStartX, _selectStartY, _selectStartX, _selectEndY);
		renderer.draw(_highlightPen, _selectStartX, _selectEndY, _selectEndX, _selectEndY);
		renderer.draw(_highlightPen, _selectEndX, _selectEndY, _selectEndX, _selectStartY);
		renderer.draw(_highlightPen, _selectEndX, _selectStartY, _selectStartX, _selectStartY);
		for (IRenderable r : _selectedObjects) {
			r.renderHighlighted(renderer);
			r.render(renderer);
		}
		if (_isDragging) {
			renderer.setOffset(0, 0);
		}
	}

	@Override
	public void begin() {
		_editor.getFrontRenderer().setRepaintListener(this);
		
		_selectPage = _editor.getDocumentEditor().getCurrentPage();
		
		_started = false;
	}
	
	/**
	 * Called on first draw() call after begin
	 * @param data
	 */
	private void begin(final DataPoint data) {
		if ((data.getX() >= Math.min(_selectStartX, _selectEndX))
				&& (data.getX() <= Math.max(_selectStartX, _selectEndX))
				&& (data.getY() >= Math.min(_selectStartY, _selectEndY))
				&& (data.getY() <= Math.max(_selectStartY, _selectEndY))) {
			_dragStartX = data.getX();
			_dragStartY = data.getY();
			_isDragging = true;
		} else {
			_selectStartX = data.getX();
			_selectStartY = data.getY();
			_isDragging = false;
		}
	}

	@Override
	public void draw(final DataPoint data) {
		if (_selectPage != null) {
			if (!_started) {
				begin(data);
				_started = true;
			}
			if (_isDragging) {
				_dragEndX = data.getX();
				_dragEndY = data.getY();
				_editor.getFrontRenderer().requireRepaint();
			} else if (_checkOnlyBoundaries) {
				_selectEndX = data.getX();
				_selectEndY = data.getY();
				_selectedObjects.clear();
				_editor.getPageEditor().suspendRepaint();
				float minX = Math.min(_selectStartX, _selectEndX);
				float minY = Math.min(_selectStartY, _selectEndY);
				float maxX = Math.max(_selectStartX, _selectEndX);
				float maxY = Math.max(_selectStartY, _selectEndY);
				_selectPage.collideWith(
						minX,
						minY,
						maxX,
						maxY,
						this);
				_editor.getFrontRenderer().requireRepaint();
				_editor.getPageEditor().resumeRepaint();
			} else {
				_editor.getPageEditor().suspendRepaint();
				final CollisionInfo collisionInfo = new CollisionInfo(data.getX(), data.getY(),
						data.getXOrig(), data.getYOrig(),
						data.getX() / data.getXOrig() * _pen.getThickness() / 2.0f,
						data.getY() / data.getYOrig() * _pen.getThickness() / 2.0f,
						_pen.getThickness(), _pen.getThickness(),
						_checkOnlyBoundaries);
				_selectPage.collideWith(collisionInfo, this);
				_editor.getPageEditor().resumeRepaint();
			}
		}
	}
	
	@Override
	public void collides(final IRenderable data) {
		System.out.println("Select " + data.getId());
		_selectedObjects.addLast(data);
	}

	@Override
	public void end() {
		_editor.getPageEditor().suspendRepaint();
		
		if (_isDragging) {
			float offsetX = _dragEndX - _dragStartX;
			float offsetY = _dragEndY - _dragStartY;
			_editor.getDocumentEditor().getHistory().beginActionGroup();
			for (IRenderable renderable : _selectedObjects) {
				_selectPage.replaceRenderable(renderable, renderable.cloneRenderable(_selectPage, offsetX, offsetY));
			}
			_editor.getDocumentEditor().getHistory().endActionGroup();
		}
		
		if (_selectedObjects.isEmpty()) {
			_editor.getFrontRenderer().setRepaintListener(null);
		}
		
		_editor.getPageEditor().resumeRepaint();
	}

	@Override
	protected Cursor generateCursor() {
		if (_pen == null) {
			return null;
		}
		final int diameter = (int) Math.max(_pen.getThickness(), 2);
		final int extraline = 3;
		final BufferedImage img = createBitmap(diameter + 2 * extraline + 1,
				diameter + 2 * extraline + 1);
		final Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.BLACK);
		g.drawLine(0, diameter / 2 + extraline, diameter + extraline * 2,
				diameter / 2 + extraline);
		g.drawLine(diameter / 2 + extraline, 0, diameter / 2 + extraline,
				diameter + extraline * 2);
		g.setPaint(_pen.getColor());
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
