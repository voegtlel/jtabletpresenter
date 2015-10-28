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
	 * Offset inside the surface to transform data.
	 */
	public float surfaceRelativeOffsetXNormalized;
	/**
	 * Offset inside the surface to transform data.
	 */
	public float surfaceRelativeOffsetYNormalized;
	
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
	public float surfaceScale = 1.0f;
	
	/**
	 * Defines the factor for scaling in x and y direction for mapping [0, 1] -> inner coordinates.
	 */
	public float innerFactorX = 1.0f;
	/**
	 * Defines the factor for scaling in x and y direction for mapping [0, 1] -> inner coordinates.
	 */
	public float innerFactorY = 1.0f;
	
	/**
	 * If null, the ratio is the full screen. Otherwise this is used as the desired ratio
	 */
	private Float _desiredRatio;
	
	/**
	 * Base size of a stroke.
	 */
	public float strokeSize = 1.0f;
	
	/**
	 * Base size of a stroke before transforming by screen size.
	 */
	private float _strokeBaseSize = 0.05f;
	
	/**
	 * If true, then the stroke size is determined by the screen size
	 */
	private boolean _strokeByScreen = false;
	
	/**
	 * Specifies if the metric has changed
	 */
	private boolean _changed = false;
	
	/**
	 * Updates the metrics and returns if something changed since the last call.
	 * @param screenWidth
	 * @param screenHeight
	 * @param desiredRatio
	 * @return
	 */
	public boolean update(final int screenWidth, final int screenHeight, final Float desiredRatio) {
		if (this.screenWidth != screenWidth || this.screenHeight != screenHeight || (_desiredRatio != desiredRatio && (_desiredRatio != null && !_desiredRatio.equals(desiredRatio)))) {
			_changed = true;
		}
		
		if (_changed) {
			_changed = false;
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			_desiredRatio = desiredRatio;
			if (desiredRatio == null) {
				surfaceVirtualWidth = screenWidth * surfaceScale;
				surfaceVirtualHeight = screenHeight * surfaceScale;
			} else {
				surfaceVirtualWidth = Math.min((int)(screenHeight * desiredRatio), screenWidth) * surfaceScale;
				surfaceVirtualHeight = Math.min((int)(screenWidth / desiredRatio), screenHeight) * surfaceScale;
			}
			innerFactorX = surfaceVirtualWidth;
			innerFactorY = surfaceVirtualHeight;
			surfaceWidth = Math.min((int)surfaceVirtualWidth, screenWidth);
			surfaceHeight = Math.min((int)surfaceVirtualHeight, screenHeight);
			surfaceDrawOffsetX = (screenWidth - surfaceWidth) / 2;
			surfaceDrawOffsetY = (screenHeight - surfaceHeight) / 2;
			// Min:
			// (surfaceRelativeOffsetXNormalized + 0.5f) * innerFactorX - surfaceWidth * 0.5f = 0
			// surfaceRelativeOffsetXNormalized = (surfaceWidth * 0.5f) / innerFactorX - 0.5f
			// Max:
			// (surfaceRelativeOffsetXNormalized + 0.5f) * innerFactorX + surfaceWidth * 0.5f = surfaceVirtualWidth
			// surfaceRelativeOffsetXNormalized = (surfaceVirtualWidth - surfaceWidth * 0.5f) / innerFactorX - 0.5f
			surfaceRelativeOffsetXNormalized = Math.max(Math.min(surfaceRelativeOffsetXNormalized,
					(surfaceVirtualWidth - surfaceWidth * 0.5f) / innerFactorX - 0.5f),
					(surfaceWidth * 0.5f) / innerFactorX - 0.5f);
			surfaceRelativeOffsetYNormalized = Math.max(Math.min(surfaceRelativeOffsetYNormalized,
					(surfaceVirtualHeight - surfaceHeight * 0.5f) / innerFactorY - 0.5f),
					(surfaceHeight * 0.5f) / innerFactorY - 0.5f);
			surfaceRelativeOffsetX = (surfaceRelativeOffsetXNormalized + 0.5f) * innerFactorX - surfaceWidth * 0.5f;
			surfaceRelativeOffsetY = (surfaceRelativeOffsetYNormalized + 0.5f) * innerFactorY - surfaceHeight * 0.5f;
			surfaceVirtualOffsetX = -surfaceRelativeOffsetX + surfaceDrawOffsetX;
			surfaceVirtualOffsetY = -surfaceRelativeOffsetY + surfaceDrawOffsetY;
			System.out.println("Offs: " + surfaceVirtualOffsetX + ", " + surfaceVirtualOffsetY);
			innerOffsetX = -surfaceRelativeOffsetX;
			innerOffsetY = -surfaceRelativeOffsetY;
			if (_strokeByScreen) {
				strokeSize = _strokeBaseSize * ((surfaceVirtualWidth + surfaceVirtualHeight)/2.0f);
			} else {
				strokeSize = _strokeBaseSize;
			}
			return true;
		}
		return false;
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
		surfaceRelativeOffsetXNormalized = src.surfaceRelativeOffsetXNormalized;
		surfaceRelativeOffsetYNormalized = src.surfaceRelativeOffsetYNormalized;
		innerOffsetX = src.innerOffsetX;
		innerOffsetY = src.innerOffsetY;
		surfaceScale = src.surfaceScale;
		innerFactorX = src.innerFactorX;
		innerFactorY = src.innerFactorY;
		
		strokeSize = src.strokeSize;
		_strokeBaseSize = src._strokeBaseSize;
		_strokeByScreen = src._strokeByScreen;
		
		_desiredRatio = src._desiredRatio;
	}
	
	/**
	 * Gets the desired ratio
	 * @return
	 */
	public Float getDesiredRatio() {
		return _desiredRatio;
	}

	/**
	 * Updates the inner scale
	 * @param relativeScale
	 */
	public void zoom(final float factor) {
		zoomAt(factor, 0.5f, 0.5f);
	}
	
	public void pan(final float x, final float y) {
		surfaceRelativeOffsetXNormalized += x;
		surfaceRelativeOffsetYNormalized += y;
		_changed = true;
	}

	public void zoomAt(final float factor, final float x, final float y) {
		//surfaceRelativeOffsetXNormalized += x * (surfaceScale - surfaceScale * factor);
		//surfaceRelativeOffsetYNormalized += y * (surfaceScale - surfaceScale * factor);
		surfaceScale *= factor;
		surfaceScale = Math.max(surfaceScale, 1.0f);
		System.out.println("ZoomAt: " + surfaceScale + " @" + surfaceRelativeOffsetXNormalized + "," + surfaceRelativeOffsetYNormalized);
		_changed = true;
	}

	public void resetView() {
		surfaceRelativeOffsetXNormalized = 0;
		surfaceRelativeOffsetYNormalized = 0;
		surfaceScale = 1.0f;
		_changed = true;
	}
	
	public void setStroke(final boolean byScreenSize, final float baseSize) {
		_strokeByScreen = byScreenSize;
		_strokeBaseSize = baseSize;
		_changed = true;
	}
}
