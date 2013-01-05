/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuSelectFrame;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;

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

	protected ToolScribble _toolScribble;

	protected ToolEraser _toolEraser;
	protected ToolEraser _toolDeleter;
	
	protected ToolImage _toolImage;

	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonSelectTool(final IToolPageEditor editor,
			final String text, final String imageResource) {
		super(editor, text, imageResource);
		_tool = new JPageToolMenuSelectFrame<ITool>();
		_tool.setSize(JPageToolButton.WIDTH_WIDE * 1,
				JPageToolButton.HEIGHT_NORMAL * 4);
		_toolScribble = new ToolScribble(_editor);
		_toolEraser = new ToolEraser(_editor, false);
		_toolDeleter = new ToolEraser(_editor, true);
		_toolImage = new ToolImage(_editor);
		_tool.addValue("Pen", "/buttons/edit-scribble.png", _toolScribble);
		_tool.addValue("Eraser", "/buttons/edit-erase.png", _toolEraser);
		_tool.addValue("Deleter", "/buttons/edit-delete.png", _toolDeleter);
		_tool.addValue("Image", "/buttons/edit-image.png", _toolImage);
	}

	@Override
	public void perform(final Component button) {
		final ITool currentSelectedTool = getSelectedTool();
		_tool.setSelectedValue(currentSelectedTool);
		_tool.showAt(button, button.getWidth(), 0);
		final ITool selectedTool = _tool.getSelectedValue();
		if (selectedTool == _toolImage) {
			// Select image to insert
			JFileChooser fileChooser = new JFileChooser();
			FileFilter imageFilter = new FileFilter() {
				@Override
				public String getDescription() {
					return "Images (.jpeg, .jpg, .png, .gif, .tiff, .tif)";
				}
				
				@Override
				public boolean accept(File f) {
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
			if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
				_editor.getDocumentEditor().setCurrentImageFile(fileChooser.getSelectedFile());
			}
		}
		if (!selectedTool.equals(currentSelectedTool)) {
			setSelectedTool(selectedTool);
		}
	}

	protected abstract void setSelectedTool(ITool tool);

	protected abstract ITool getSelectedTool();
}
