package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Color;

import de.freiburg.uni.tablet.presenter.editor.IPageEditor;

public interface IPageRenderer extends IPageEditor, IDisplayRenderer {

	/**
	 * Gets the color for void space (outside of aspect ratio)
	 * @return
	 */
	Color getVoidColor();
	
	/**
	 * Sets the color for void space (outside of aspect ratio)
	 * @param voidColor
	 */
	void setVoidColor(Color voidColor);

}