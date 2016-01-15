/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import javax.swing.JOptionPane;

import de.freiburg.uni.tablet.presenter.document.document.DocumentServer;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonDocumentNew extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonDocumentNew(final IToolPageEditor editor) {
		super("new", editor, "New", "/buttons/document-new");
	}

	@Override
	public void performLater(final Component component) {
		int result = JOptionPane.showConfirmDialog(component, "Save current document before creating a new document?", "New Document...", JOptionPane.YES_NO_CANCEL_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			if (!FileHelper.showSaveDialog(component, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("save.defaultExt", "jpd")))) {
				return;
			}
		} else if (result == JOptionPane.CANCEL_OPTION) {
			return;
		}
		_editor.getDocumentEditor().setDocument(new DocumentServer(1));
	}
}
