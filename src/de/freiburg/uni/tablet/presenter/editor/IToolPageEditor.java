/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JFrame;

import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditorClient;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

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
	IDocumentEditorClient getDocumentEditor();

	/**
	 * Gets the front renderer
	 * 
	 * @return
	 */
	IPageBackRenderer getFrontRenderer();

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
	 * Gets the main dialog
	 * @return
	 */
	JFrame getMainComponent();
	
	/**
	 * Adds a component (hidden below the rendering canvas)
	 * @param c
	 */
	void addDummyComponent(Component c);

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
	
	/**
	 * Gets if the editor is connected to a networking page
	 * @return
	 */
	boolean isConnectedUp();
	
	/**
	 * Gets if the editor is connected to a networking page
	 * @return
	 */
	boolean isConnectedDown();
	
	/**
	 * Disconnect all networking
	 */
	void disconnect();

	/**
	 * Connect as uploading client
	 * @param hostname
	 * @param port
	 * @param initDown
	 * @param authToken
	 * @throws IOException
	 */
	void connectUpClient(String hostname, int port, boolean initDown,
			String name, String authToken) throws IOException;

	/**
	 * Connect as downloading client
	 * @param hostname
	 * @param port
	 * @param authToken
	 * @throws IOException
	 */
	void connectDownClient(String hostname, int port, String name, String authToken)
			throws IOException;
}
