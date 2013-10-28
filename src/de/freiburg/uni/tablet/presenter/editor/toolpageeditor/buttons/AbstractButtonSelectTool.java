/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;
import de.freiburg.uni.tablet.presenter.tools.ToolSelectMove;
import de.freiburg.uni.tablet.presenter.tools.ToolText;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonSelectTool extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final ToolScribble _toolScribble;
	protected final ToolScribble _toolLine;

	protected final ToolEraser _toolEraser;
	protected final ToolEraser _toolDeleter;
	
	protected final ToolImage _toolImage;
	
	protected final ToolText _toolText;
	
	protected final ToolSelectMove _toolSelectMove;
	
	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonSelectTool(final String name, final IToolPageEditor editor,
			final String text, final String imageResource) {
		super(name, editor, text, imageResource, 7);
		_toolScribble = new ToolScribble(_editor, false);
		_toolLine = new ToolScribble(_editor, true);
		_toolEraser = new ToolEraser(_editor, false);
		_toolDeleter = new ToolEraser(_editor, true);
		_toolImage = new ToolImage(_editor);
		_toolText = new ToolText(_editor);
		_toolSelectMove = new ToolSelectMove(_editor, true);
		this._frame.addItem(new ButtonToolItem(editor, this, "scribble", "Pen", "/buttons/edit-scribble.png", _toolScribble), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "line", "Line", "/buttons/edit-line.png", _toolLine), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "eraser", "Eraser", "/buttons/edit-erase.png", _toolEraser), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "deleter", "Deleter", "/buttons/edit-delete.png", _toolDeleter), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "image", "Image", "/buttons/edit-image.png", _toolImage), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "text", "Text", "/buttons/edit-text.png", _toolText), true);
		this._frame.addItem(new ButtonToolItem(editor, this, "selectMove", "Drag", "/buttons/edit-drag.png", _toolSelectMove), true);
	}
	
	public ITool getTool(final String name) {
		final IButtonAction button = this.getButton(name);
		if (button == null) {
			return null;
		}
		return ((ButtonToolItem)button).getTool();
	}

	public abstract void setSelectedTool(final ITool tool);

	public abstract ITool getSelectedTool();
}
