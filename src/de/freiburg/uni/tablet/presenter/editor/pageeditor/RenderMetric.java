package de.freiburg.uni.tablet.presenter.editor.pageeditor;

public class RenderMetric {
	/**
	 * Size of the screen (full drawing surface)
	 */
	public int screenWidth;
	/**
	 * Size of the screen (full drawing surface)
	 */
	public int screenHeight;
	
	/**
	 * Size of the full surface (might contain areas outside of the actual screen).
	 */
	public float surfaceVirtualWidth;
	/**
	 * Size of the full surface (might contain areas outside of the actual screen).
	 */
	public float surfaceVirtualHeight;
	
	/**
	 * Offset of the full surface (might contain areas outside of the actual screen).
	 */
	public float surfaceVirtualOffsetX;
	/**
	 * Offset of the full surface (might contain areas outside of the actual screen).
	 */
	public float surfaceVirtualOffsetY;
	
	/**
	 * Size of the target rendering surface (after applying ratio)
	 */
	public int surfaceWidth;
	/**
	 * Size of the target rendering surface (after applying ratio)
	 */
	public int surfaceHeight;
	/**
	 * Offset of the target rendering surface (after applying ratio).
	 * Specifies, where to draw the surface on the screen.
	 */
	public int surfaceDrawOffsetX;
	/**
	 * Offset of the target rendering surface (after applying ratio).
	 * Specifies, where to draw the surface on the screen.
	 */
	public int surfaceDrawOffsetY;
	
	/**
	 * Offset inside the surface to transform data.
	 */
	public float surfaceRelativeOffsetX;
	/**
	 * Offset inside the surface to transform data.
	 */
	public float surfaceRelativeOffsetY;
	
	/**
	 * Offset of the target rendering surface (after applying ratio).
	 * Specifies the offset of the left upper corner of the surface in surface inner coordinates.
	 */
	public float innerOffsetX;
	/**
	 * Offset of the target rendering surface (after applying ratio).
	 * Specifies the offset of the left upper corner of the surface in surface inner coordinates.
	 */
	public float innerOffsetY;
	/**
	 * Scale inside the surface to transform data.
	 */
	public float innerScale = 1.0f;
	
	/**
	 * Defines the factor for scaling in x and y direction for mapping [0, 1] -> inner coordinates.
	 */
	public float innerFactorX = 1.0f;
	/**
	 * Defines the factor for scaling in x and y direction for mapping [0, 1] -> inner coordinates.
	 */
	public float innerFactorY = 1.0f;
	
	private Float _desiredRatio;
	
	private boolean _changed = false;
	
	/**
	 * Updates the metrics and returns if something changed since the last call.
	 * @param screenWidth
	 * @param screenHeight
	 * @param desiredRatio
	 * @return
	 */
	public boolean update(final int screenWidth, final int screenHeight, final Float desiredRatio) {
		if (this.screenWidth != screenWidth || this.screenHeight != screenHeight || _desiredRatio != desiredRatio) {
			_changed = true;
		}
		
		if (_changed) {
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			_desiredRatio = desiredRatio;
			if (desiredRatio == null) {
				surfaceVirtualWidth = screenWidth * innerScale;
				surfaceVirtualHeight = screenHeight * innerScale;
				surfaceWidth = screenWidth;
				surfaceHeight = screenHeight;
				surfaceDrawOffsetX = 0;
				surfaceDrawOffsetY = 0;
				surfaceVirtualOffsetX = surfaceRelativeOffsetX;
				surfaceVirtualOffsetY = surfaceRelativeOffsetY;
			} else {
				surfaceVirtualWidth = Math.min((int)(screenHeight * desiredRatio), screenWidth) * innerScale;
				surfaceVirtualHeight = Math.min((int)(screenWidth / desiredRatio), screenHeight) * innerScale;
				surfaceWidth = Math.min((int)surfaceVirtualWidth, screenWidth);
				surfaceHeight = Math.min((int)surfaceVirtualHeight, screenHeight);
				surfaceDrawOffsetX = (screenWidth - surfaceWidth) / 2;
				surfaceDrawOffsetY = (screenHeight - surfaceHeight) / 2;
				
				// TODO - or +?
				surfaceVirtualOffsetX = surfaceRelativeOffsetX + surfaceDrawOffsetX;
				surfaceVirtualOffsetY = surfaceRelativeOffsetY + surfaceDrawOffsetY;
			}
			innerOffsetX = -surfaceRelativeOffsetX;
			innerOffsetY = -surfaceRelativeOffsetY;
			innerFactorX = surfaceVirtualWidth;
			innerFactorY = surfaceVirtualHeight;
			return true;
		}
		return false;
	}
	
	/**
	 * Updates the inner scale
	 * @param relativeScale
	 */
	public void scale(final float relativeScale) {
		innerScale *= relativeScale;
		innerScale = Math.min(innerScale, 1.0f);
		_changed = true;
	}

	public void copyFrom(final RenderMetric src) {
		screenWidth = src.screenWidth;
		screenHeight = src.screenHeight;
		surfaceVirtualWidth = src.surfaceVirtualWidth;
		surfaceVirtualHeight = src.surfaceVirtualHeight;
		surfaceWidth = src.surfaceWidth;
		surfaceHeight = src.surfaceHeight;
		surfaceDrawOffsetX = src.surfaceDrawOffsetX;
		surfaceDrawOffsetY = src.surfaceDrawOffsetY;
		surfaceRelativeOffsetX = src.surfaceRelativeOffsetX;
		surfaceRelativeOffsetY = src.surfaceRelativeOffsetY;
		innerOffsetX = src.innerOffsetX;
		innerOffsetY = src.innerOffsetY;
		innerScale = src.innerScale;
		innerFactorX = src.innerFactorX;
		innerFactorY = src.innerFactorY;
		
		_desiredRatio = src._desiredRatio;
	}
}
