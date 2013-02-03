/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonSelectTool implements IButton {
	private final int _groupId;
	protected final IToolPageEditor _editor;
	
	protected final ToolScribble _toolScribble;

	protected final ToolEraser _toolEraser;
	protected final ToolEraser _toolDeleter;
	
	protected final ToolImage _toolImage;
	
	/**
	 * Creates the action with an editor.
	 * 
	 * @param text
	 * @param imageResource
	 */
	public AbstractButtonSelectTool(final int groupId, final IToolPageEditor editor) {
		_groupId = groupId;
		_editor = editor;
		_toolScribble = new ToolScribble(_editor);
		_toolEraser = new ToolEraser(_editor, false);
		_toolDeleter = new ToolEraser(_editor, true);
		_toolImage = new ToolImage(_editor);
	}

	@Override
	public boolean perform(final Context context, final int actionId, final int groupActionId) {
		if (_groupId == groupActionId) {
			ITool selectedTool;
			if (actionId == R.id.tool_scribble) {
				selectedTool = _toolScribble;
			} else if (actionId == R.id.tool_eraser) {
				selectedTool = _toolEraser;
			} else if (actionId == R.id.tool_deleter) {
				selectedTool = _toolDeleter;
			} else if (actionId == R.id.tool_image) {
				selectedTool = _toolImage;
				// TODO: Show open file dialog
			} else {
				throw new IllegalStateException();
			}
			setSelectedTool(selectedTool);
			return true;
		}
		return false;
	}
	
	/*@Override
	public void performLater(Component component) {
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
			if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
				try {
					_editor.getDocumentEditor().setCurrentImageFile(fileChooser.getSelectedFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}*/
	
	public ITool getTool(final String name) {
		if (name.equals("scribble")) {
			return _toolScribble;
		} else if (name.equals("eraser")) {
			return _toolEraser;
		} else if (name.equals("deleter")) {
			return _toolDeleter;
		} else if (name.equals("image")) {
			return _toolImage;
		}
		return null;
	}

	public abstract void setSelectedTool(final ITool tool);
	
	public abstract ITool getSelectedTool();
}
