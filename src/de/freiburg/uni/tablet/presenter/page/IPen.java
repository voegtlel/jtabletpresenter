package de.freiburg.uni.tablet.presenter.page;

import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

public interface IPen extends IBinarySerializable {
	/**
	 * Get system paint.
	 * 
	 * @return paint
	 */
	int getColor();

	/**
	 * Gets the thickness.
	 * 
	 * @return thickness
	 */
	float getThickness();

	Cap getStrokeCap();

	Join getStrokeJoin();

	float getStrokeMiter();

	float getStrokeWidth();
}
