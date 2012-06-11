package de.freiburg.uni.tablet.presenter.page;

import java.awt.Paint;
import java.awt.Stroke;

import de.freiburg.uni.tablet.presenter.IBinarySerializable;

public interface IPen extends IBinarySerializable {
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
