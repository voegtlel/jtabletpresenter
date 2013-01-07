/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;

/**
 * @author lukas
 * 
 */
public interface IToolPageEditor {
	/**
	 * Gets, if the editor is in fullscreen mode
	 * 
	 * @return is fullscreen
	 */
	boolean isFullscreen();

	/**
	 * Sets the fullscreen state
	 * 
	 * @param fullscreen
	 *            fullscreen state
	 */
	void setFullscreen(boolean fullscreen);

	/**
	 * Gets the document editor
	 * 
	 * @return
	 */
	DocumentEditor getDocumentEditor();

	/**
	 * Gets the front renderer
	 * 
	 * @return
	 */
	IPageFrontRenderer getFrontRenderer();

	/**
	 * Gets the page editor
	 * 
	 * @return
	 */
	IPageEditor getPageEditor();
	
	/**
	 * Gets the document config
	 * 
	 * @return
	 */
	DocumentConfig getConfig();

	/**
	 * Sets if the toolbar should be visible
	 * @param visible
	 */
	void setToolbarVisible(boolean visible);
	
	/**
	 * Gets if the toolbar is visible
	 * @return
	 */
	boolean isToolbarVisible();
}
