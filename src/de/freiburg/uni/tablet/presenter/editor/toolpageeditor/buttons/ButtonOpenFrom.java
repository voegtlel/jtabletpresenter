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
public class ButtonOpenFrom extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	/**
	 * Creates the action with an editor.
	 */
	public ButtonOpenFrom(final IToolPageEditor editor) {
		super("open", editor, "Open", "/buttons/document-open");
	}

	@Override
	public void performLater(final Component component) {
		FileHelper.showOpenDialog(component, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("open.defaultExt", "jpd")));
	}
}
