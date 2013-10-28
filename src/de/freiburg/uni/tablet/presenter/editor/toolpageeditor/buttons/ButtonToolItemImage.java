package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class ButtonToolItemImage extends ButtonToolItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ButtonToolItemImage(final IToolPageEditor editor,
			final AbstractButtonSelectTool selectTool, final String name, final String text,
			final String imageResource, final ITool tool) {
		super(editor, selectTool, name, text, imageResource, tool);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void performLater(final Component component) {
		super.performLater(component);
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
