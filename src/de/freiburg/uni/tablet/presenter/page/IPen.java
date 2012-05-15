package de.freiburg.uni.tablet.presenter.page;

import java.awt.Paint;
import java.awt.Stroke;

public interface IPen {
	/**
	 * Get system stroke.
	 * 
	 * @return stroke
	 */
	Stroke getStroke();

	/**
	 * Get system paint.
	 * 
	 * @return paint
	 */
	Paint getPaint();

	/**
	 * Gets the thickness.
	 * 
	 * @return thickness
	 */
	float getThickness();
}
