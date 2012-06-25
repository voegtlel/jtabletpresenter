package de.freiburg.uni.tablet.presenter.editor;

import java.awt.Component;
import java.awt.Cursor;

import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBuffer;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public interface IPageEditor {
	/**
	 * Gets the normal tool
	 * 
	 * @return normal tool
	 */
	ITool getNormalTool();

	/**
	 * Sets the normal tool
	 * 
	 * @param normalTool
	 *            new normal tool
	 */
	void setNormalTool(ITool normalTool);

	/**
	 * Gets the inverted tool
	 * 
	 * @return inverted tool
	 */
	ITool getInvertedTool();

	/**
	 * Sets the inverted tool
	 * 
	 * @param invertedTool
	 *            inverted tool
	 */
	void setInvertedTool(ITool invertedTool);

	/**
	 * Method to set the currently active cursor
	 * 
	 * @param cursor
	 *            new cursor
	 */
	void setCursor(Cursor cursor);

	/**
	 * Gets the gui-container.
	 * 
	 * @return gui-container
	 */
	Component getContainerComponent();

	/**
	 * Clears the renderer
	 */
	void clear();

	/**
	 * Sets the page layer
	 * 
	 * @param pageLayer
	 */
	void setDisplayedPageLayerBuffer(IPageLayerBuffer pageLayer);

	/**
	 * Gets the page layer
	 * 
	 * @return
	 */
	IPageLayerBuffer getPageLayer();

}