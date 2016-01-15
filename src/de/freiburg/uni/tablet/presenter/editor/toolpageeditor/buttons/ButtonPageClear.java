/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonPageClear extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonPageClear(final IToolPageEditor editor) {
		super("clear", editor, "Clear", "/buttons/page-clear");
	}

	@Override
	public void performLater(final Component component) {
		_editor.getDocumentEditor().getHistory().beginActionGroup();
		DocumentPage page = _editor.getDocumentEditor().getCurrentPage();
		page.clear();
		_editor.getDocumentEditor().getHistory().endActionGroup();
	}
}
