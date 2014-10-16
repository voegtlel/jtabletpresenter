package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;
import java.awt.Stroke;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

public interface IPen extends IBinarySerializable {
	/**
	 * Get system stroke.
	 * 
	 * @return stroke
	 */
	Stroke getStroke();
	
	/**
	 * Get system stroke with pressure.
	 * 
	 * @return stroke
	 */
	Stroke getStroke(float pressure);

	/**
	 * Get system paint.
	 * 
	 * @return paint
	 */
	Color getColor();

	/**
	 * Gets the thickness.
	 * 
	 * @return thickness
	 */
	float getThickness();

	/**
	 * Gets the thickness for the specified pressure
	 * @param pressure
	 * @return
	 */
	float getThickness(float pressure);
}
