package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;
import java.awt.Stroke;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;

public interface IPen extends IBinarySerializable {
	/**
	 * Pressure for a non-variable mode
	 */
	public static final float DEFAULT_PRESSURE = 0.5f;
	
	public static final float MAX_PRESSURE = 1.0f;
	
	/**
	 * Get system stroke.
	 * 
	 * @return stroke
	 */
	Stroke getStroke(RenderMetric metric);
	
	/**
	 * Get system stroke with pressure.
	 * 
	 * @return stroke
	 */
	Stroke getStroke(RenderMetric metric, float pressure);

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
	 * Gets the thickness for the specified pressure and a render metric
	 * @param metric
	 * @param pressure
	 * @return
	 */
	float getThickness(RenderMetric metric, float pressure);
}
