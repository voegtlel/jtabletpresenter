package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
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

	/**
	 * @param editor
	 * @param checkOnlyBoundaries
	 */
	public ToolEraser(final IToolPageEditor editor, final boolean checkOnlyBoundaries) {
		super(editor);
		_pen = new SolidPen(editor.getConfig().getFloat("editor.eraser.thickness", 15f), new Color(0xFF, 0xFF, 0xFF,
				0xAA));
		_checkOnlyBoundaries = checkOnlyBoundaries;
		updateCursor();
	}
	
	@Override
	public void render(final IPageBackRenderer renderer) {
	}

	@Override
	public void begin() {
		_editor.getDocumentEditor().getHistory().beginActionGroup();
		
		_eraseInfo = new EraseInfo();
		_erasePage = _editor.getDocumentEditor().getCurrentPage();
		_replacedObjects.clear();
		
		_editor.getFrontRenderer().setRepaintListener(this);
		
		fireToolStart();
	}

	@Override
	public void draw(final DataPoint data) {
		if (_erasePage != null) {
			_editor.getPageEditor().suspendRepaint();
			float thickness = _pen.getThickness();
			float xReal = data.getX() * _editor.getPageEditor().getRenderMetric().innerFactorX;
			float yReal = data.getY() * _editor.getPageEditor().getRenderMetric().innerFactorY;
			_eraseInfo.createCollisionInfo(data.getX(), data.getY(),
					xReal, yReal,
					data.getX() / xReal * thickness / 2.0f,
					data.getY() / yReal * thickness / 2.0f,
					thickness, thickness,
					_checkOnlyBoundaries, data, _editor.getConfig().getBoolean("editor.eraser.fastCollide", true));
			_erasePage.collideWith(_eraseInfo.getCollisionInfo(), this);
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
				fireToolFinish(renderable);
			}
		}
		
		_editor.getDocumentEditor().getHistory().endActionGroup();
		
		_eraseInfo = null;
		_replacedObjects.clear();
		
		_editor.getFrontRenderer().setRepaintListener(null);

		_editor.getPageEditor().resumeRepaint();
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
