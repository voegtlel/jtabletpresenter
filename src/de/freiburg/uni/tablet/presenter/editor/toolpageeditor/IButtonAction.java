/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

/**
 * @author lukas
 * 
 */
public interface IButtonAction {
	/**
	 * Performs the action
	 */
	void perform(Component button);

	/**
	 * Text for the button.
	 * 
	 * @return
	 */
	String getText();

	/**
	 * Path to the image resource.
	 * 
	 * @return
	 */
	String getImageResource();

	/**
	 * If null, a button will be created and perform will be used as action. If
	 * available, gets the custom control, events must be registered by this
	 * instance.
	 * 
	 * @return control
	 */
	Component getControl();

	/**
	 * Called, when the editor object was changed
	 */
	void onUpdateEditor(DocumentEditor lastEditor);
}
