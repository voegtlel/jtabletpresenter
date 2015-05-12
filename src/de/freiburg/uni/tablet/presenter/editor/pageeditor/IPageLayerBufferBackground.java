package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import de.freiburg.uni.tablet.presenter.document.IEntity;

/**
 * Interface for pdf layer buffer
 * @author Luke
 *
 */
public interface IPageLayerBufferBackground extends IPageLayerBuffer {

	/**
	 * Sets the document
	 * @param document
	 */
	void setBackgroundEntity(IEntity backgroundEntity);

	/**
	 * Enable or disable automatic page ratio (used for getDesiredRatio())
	 * @param enabled
	 */
	void setRatioEnabled(boolean enabled);

	/**
	 * Checks if the automatic ratio is enabled (used for getDesiredRatio())
	 * @return
	 */
	boolean isRatioEnabled();

}