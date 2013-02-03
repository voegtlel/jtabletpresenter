package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.AbstractRenderable;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.Scribble;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class ToolScribble extends AbstractTool {
	private Scribble _scribble = null;
	
	/**
	 * 
	 * @param container
	 *            used for cursor changing
	 */
	public ToolScribble(final IToolPageEditor editor) {
		super(editor);
	}

	@Override
	synchronized public void begin() {
		_scribble = new Scribble(_editor.getDocumentEditor().getCurrentPage(), _editor.getDocumentEditor().getCurrentPen());
		_editor.getFrontRenderer().setRepaintListener(this);
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
			_scribble.addPoint(data);
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
}
