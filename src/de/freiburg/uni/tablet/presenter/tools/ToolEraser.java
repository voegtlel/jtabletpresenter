package de.freiburg.uni.tablet.presenter.tools;

import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.CollisionListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolEraser extends AbstractTool implements CollisionListener {
	private final IPen _pen;

	private EraseInfo _eraseInfo = null;
	private DocumentPage _erasePage = null;
	private final HashMap<Long, IRenderable> _replacedObjects = new HashMap<Long, IRenderable>();

	private boolean _checkOnlyBoundaries = false;
	
	private Object _drawSync = new Object();
	private float _eraserLocationX = 0;
	private float _eraserLocationY = 0;

	/**
	 * @param editor
	 * @param checkOnlyBoundaries
	 */
	public ToolEraser(final IToolPageEditor editor, boolean checkOnlyBoundaries) {
		super(editor);
		_pen = new SolidPen(editor.getConfig().getFloat("editor.eraser.thickness", 50f), 0xffffffff);
		_checkOnlyBoundaries = checkOnlyBoundaries;
	}
	
	@Override
	public void render(final IPageBackRenderer renderer) {
		float eraserLocationX;
		float eraserLocationY;
		synchronized (_drawSync) {
			eraserLocationX = _eraserLocationX;
			eraserLocationY = _eraserLocationY;
		}
		renderer.drawEraser(_pen, eraserLocationX, eraserLocationY);
	}

	@Override
	public void begin() {
		_editor.getDocumentEditor().getHistory().beginActionGroup();
		
		_eraseInfo = new EraseInfo();
		_erasePage = _editor.getDocumentEditor().getCurrentPage();
		_replacedObjects.clear();
		
		_editor.getFrontRenderer().setRepaintListener(this);
	}

	@Override
	public void draw(final DataPoint data) {
		if (_erasePage != null) {
			_editor.getPageEditor().suspendRepaint();
			synchronized (_drawSync) {
				_eraserLocationX = data.getX();
				_eraserLocationY = data.getY();
			}
			_eraseInfo.createCollisionInfo(data.getX(), data.getY(),
					data.getXOrig(), data.getYOrig(),
					data.getX() / data.getXOrig() * _pen.getThickness() / 2.0f,
					data.getY() / data.getYOrig() * _pen.getThickness() / 2.0f,
					_pen.getThickness(), _pen.getThickness(),
					_checkOnlyBoundaries);
			_erasePage.collideWith(_eraseInfo.getCollisionInfo(), this);
			_editor.getFrontRenderer().requireRepaint();
			_editor.getPageEditor().resumeRepaint();
		}
	}
	
	@Override
	public void collides(final IRenderable data) {
		IRenderable replacedInstance = _replacedObjects.get(data.getId());
		if (replacedInstance == null) {
			replacedInstance = data.cloneRenderable(_erasePage);
			final IRenderable prevRenderable = _erasePage.removeRenderable(data);
			if (replacedInstance.eraseStart(_eraseInfo)) {
				_replacedObjects.put(replacedInstance.getId(), replacedInstance);
				_erasePage.insertRenderable(prevRenderable, replacedInstance);
			} else {
				replacedInstance = null;
			}
		}
		if (replacedInstance != null) {
			if (!replacedInstance.eraseAt(_eraseInfo)) {
				_replacedObjects.remove(replacedInstance.getId());
				_erasePage.removeRenderable(replacedInstance);
			}
		}
	}

	@Override
	public void end() {
		_editor.getPageEditor().suspendRepaint();
		for (IRenderable renderable : _replacedObjects.values()) {
			if (!renderable.eraseEnd(_eraseInfo)) {
				_erasePage.removeRenderable(renderable);
			}
		}
		
		_editor.getDocumentEditor().getHistory().endActionGroup();
		
		_eraseInfo = null;
		_replacedObjects.clear();
		
		_editor.getFrontRenderer().setRepaintListener(null);

		_editor.getPageEditor().resumeRepaint();
	}
}
