/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.*;

/**
 * @author lukas
 * 
 */
public interface IButtonAction {
	/**
	 * Performs the action
	 */
	void perform(Point desiredLocation);
	
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
	 * The loaded image resource.
	 * 
	 * @return
	 */
	Image getImageResource(int width, int height);

	/**
	 * If null, a button will be created and perform will be used as action. If
	 * available, gets the custom control, events must be registered by this
	 * instance.
	 * 
	 * @return control
	 */
	Component getControl();
	
	/**
	 * Gets the button with the given name
	 * @param name
	 * @return
	 */
	IButtonAction getButton(String name);
	
	/**
	 * Releases all resources
	 */
	void dispose();
}
