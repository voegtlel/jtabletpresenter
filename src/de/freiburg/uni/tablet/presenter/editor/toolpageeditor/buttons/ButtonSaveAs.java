/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonSaveAs extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Creates the action with an editor.
	 */
	public ButtonSaveAs(final IToolPageEditor editor) {
		super("save", editor, "Save", "/buttons/document-save-as");
	}
	
	

	@Override
	public void performLater(final Component component) {
		if (FileHelper.showSaveDialog(component, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("save.defaultExt", "jpd")))) {
			_editor.wasSaved();
		}
	}
}
