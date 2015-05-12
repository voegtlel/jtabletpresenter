package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class ToolZoomPan extends AbstractTool {
	private float _dragStartX = 0f;
	private float _dragStartY = 0f;
	private boolean _hasStart = false;
	private boolean _zoom;
	
	/**
	 * @param editor
	 * @param checkOnlyBoundaries
	 */
	public ToolZoomPan(final IToolPageEditor editor, final boolean zoom) {
		super(editor);
		_zoom = zoom;
		updateCursor();
	}
	
	@Override
	public void render(final IPageBackRenderer renderer) {
	}

	@Override
	public void begin() {
		_hasStart = false;
	}

	@Override
	public void draw(final DataPoint data) {
		if (!_hasStart) {
			_dragStartX = data.getXOrig();
			_dragStartY = data.getYOrig();
			_hasStart = true;
		}
		
		float relX = data.getXOrig() - _dragStartX;
		float relY = data.getYOrig() - _dragStartY;
		if (_zoom) {
			relX /= _editor.getPageEditor().getRenderMetric().innerFactorX;
			float relZoom = (float)Math.exp(relX * 0.1f);
			System.out.println("RelZoom: " + relZoom);
			_editor.getPageEditor().zoom(relZoom);
		} else {
			relX /= _editor.getPageEditor().getRenderMetric().innerFactorX;
			relY /= _editor.getPageEditor().getRenderMetric().innerFactorY;
			_editor.getPageEditor().pan(relX, relY);
		}
		
		_dragStartX = data.getXOrig();
		_dragStartY = data.getYOrig();
	}
	
	@Override
	public void end() {
		_hasStart = false;
	}
	
	@Override
	public void updateTool() {
	}

	@Override
	protected Cursor generateCursor() {
		return null;
	}
}
