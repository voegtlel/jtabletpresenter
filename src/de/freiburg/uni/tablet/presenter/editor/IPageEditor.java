package de.freiburg.uni.tablet.presenter.editor;

import java.awt.Component;
import java.awt.Cursor;

import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBuffer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;
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
	 * Sets the normal tool for only one operation
	 * (then it is reverted to the previous tool)
	 * 
	 * @param normalTool
	 *            new normal tool
	 */
	void setNormalToolOnce(ITool normalTool);

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
	void setToolCursor(Cursor cursor);
	
	/**
	 * Sets a temporary cursor, that overrides the tool cursor.
	 * @param cursor
	 */
	void setTemporaryCursor(Cursor cursor);
	
	/**
	 * Resets the temporary cursor and activates the tool cursor.
	 * @param cursor
	 */
	void resetTemporaryCursor();

	/**
	 * Gets the gui-container.
	 * 
	 * @return gui-container
	 */
	Component getContainerComponent();

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

	/**
	 * Stops the active tool
	 */
	void stopTool();
	
	/**
	 * Updates the tools (called when the elements on the document were modified otherwise)
	 * TODO: call and implement in selection
	 */
	void updateTool();
	
	/**
	 * Require repaint
	 */
	void requireRepaint();

	/**
	 * Suspends repainting
	 */
	void suspendRepaint();
	
	/**
	 * Resumes repainting
	 */
	void resumeRepaint();

	/**
	 * Zooms the view
	 * @param factor
	 * @param x
	 * @param y
	 */
	void zoomAt(float factor, float x, float y);
	
	/**
	 * Pans the view
	 * @param x
	 * @param y
	 */
	void pan(float x, float y);
	
	/**
	 * Zooms the view
	 * @param factor
	 */
	void zoom(float factor);

	/**
	 * Resets the view
	 */
	void resetZoomPan();

	/**
	 * Gets the render metric
	 * @return
	 */
	RenderMetric getRenderMetric();
}
