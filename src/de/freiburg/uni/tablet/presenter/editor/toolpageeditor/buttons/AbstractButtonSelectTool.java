/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuSelectFrame;
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
public abstract class AbstractButtonSelectTool extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JPageToolMenuSelectFrame<ITool> _tool;

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
		super(name, editor, text, imageResource);
		_tool = new JPageToolMenuSelectFrame<ITool>();
		_tool.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * 7);
		_toolScribble = new ToolScribble(_editor, false);
		_toolLine = new ToolScribble(_editor, true);
		_toolEraser = new ToolEraser(_editor, false);
		_toolDeleter = new ToolEraser(_editor, true);
		_toolImage = new ToolImage(_editor);
		_toolText = new ToolText(_editor);
		_toolSelectMove = new ToolSelectMove(_editor, true);
		_tool.addValue("Pen", "/buttons/edit-scribble.png", _toolScribble);
		_tool.addValue("Line", "/buttons/edit-line.png", _toolLine);
		_tool.addValue("Eraser", "/buttons/edit-erase.png", _toolEraser);
		_tool.addValue("Deleter", "/buttons/edit-delete.png", _toolDeleter);
		_tool.addValue("Image", "/buttons/edit-image.png", _toolImage);
		_tool.addValue("Text", "/buttons/edit-text.png", _toolText);
		_tool.addValue("Drag", "/buttons/edit-drag.png", _toolSelectMove);
	}

	@Override
	public void perform(final Point desiredLocation) {
		final ITool currentSelectedTool = getSelectedTool();
		_tool.setSelectedValue(currentSelectedTool);
		_tool.showAt(desiredLocation);
		final ITool selectedTool = _tool.getSelectedValue();
		if (!selectedTool.equals(currentSelectedTool)) {
			setSelectedTool(selectedTool);
		}
		super.perform(desiredLocation);
	}
	
	@Override
	public void performLater(final Component component) {
		final ITool selectedTool = _tool.getSelectedValue();
		if (selectedTool == _toolImage) {
			// Select image to insert
			final JFileChooser fileChooser = new JFileChooser();
			final FileFilter imageFilter = new FileFilter() {
				@Override
				public String getDescription() {
					return "Images (.jpeg, .jpg, .png, .gif, .tiff, .tif)";
				}
				
				@Override
				public boolean accept(final File f) {
					if (!f.isFile()) {
						return true;
					}
					String path = f.getPath().toLowerCase();
					return path.endsWith(".tiff")
							|| path.endsWith(".tif")
							|| path.endsWith(".gif")
							|| path.endsWith(".jpeg")
							|| path.endsWith(".jpg")
							|| path.endsWith(".png")
							|| path.endsWith(".bmp");
				}
			};
			fileChooser.addChoosableFileFilter(imageFilter);
			fileChooser.setFileFilter(imageFilter);
			if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
				try {
					_editor.getDocumentEditor().setCurrentImageFile(fileChooser.getSelectedFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public ITool getTool(final String name) {
		if (name.equals("scribble")) {
			return _toolScribble;
		} else if (name.equals("line")) {
			return _toolLine;
		} else if (name.equals("eraser")) {
			return _toolEraser;
		} else if (name.equals("deleter")) {
			return _toolDeleter;
		} else if (name.equals("image")) {
			return _toolImage;
		} else if (name.equals("selectMove")) {
			return _toolSelectMove;
		}
		return null;
	}

	public abstract void setSelectedTool(final ITool tool);

	public abstract ITool getSelectedTool();
}
