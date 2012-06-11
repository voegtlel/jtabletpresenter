package de.freiburg.uni.tablet.presenter.editor;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public interface IPageEditor {
	/**
	 * Gets the currently active page.
	 * 
	 * @return active page
	 */
	IPage getPage();

	/**
	 * Sets the currently active page
	 * 
	 * @param page
	 *            new active page
	 */
	void setPage(IPage page);

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
	 * Gets the gui-container.
	 * 
	 * @return gui-container
	 */
	Component getContainer();

}