package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ToolZoomPan;

public class ActionZoomPan extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int MODE_ZOOM = 0;
	public static final int MODE_PAN = 2;
	public static final int MODE_RESET = 4;

	private int _mode;

	/**
	 * Creates the action with an editor.
	 */
	public ActionZoomPan(final IToolPageEditor editor, final String name, final int mode) {
		super(name, editor, "Zoom/Pan", "/buttons/action-zoom.png");
		_mode = mode;
	}

	@Override
	public void performLater(final Component component) {
		switch (_mode) {
		case MODE_ZOOM:
			System.out.println("Zoom");
			if (!(_editor.getPageEditor().getNormalTool() instanceof ToolZoomPan) || !((ToolZoomPan)_editor.getPageEditor().getNormalTool()).isZoom()) {
				_editor.getPageEditor().setNormalToolOnce(new ToolZoomPan(_editor, true));
			}
			break;
			
		case MODE_PAN:
			System.out.println("Pan");
			if (!(_editor.getPageEditor().getNormalTool() instanceof ToolZoomPan) || ((ToolZoomPan)_editor.getPageEditor().getNormalTool()).isZoom()) {
				_editor.getPageEditor().setNormalToolOnce(new ToolZoomPan(_editor, false));
			}
			break;
			
		case MODE_RESET:
			_editor.getPageEditor().resetZoomPan();
			break;
		}
	}
}
