package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.MotionEvent;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class PagePenDispatcher {
	private ITool _normalTool;
	private ITool _invertedTool;

	private int _activePenKind;
	private int _activePenButton;
	private ITool _activeTool;

	private ITool _hoverTool;

	private int _penKind = MotionEvent.TOOL_TYPE_UNKNOWN;

	private DataPoint _lastData = null;

	private float _drawSizeX = 0;
	private float _drawSizeY = 0;

	public PagePenDispatcher() {
	}

	/**
	 * Reads the data point
	 * 
	 * @param pen
	 * @param timestamp
	 * @return
	 */
	private DataPoint getDataPoint(final MotionEvent event) {
		return new DataPoint(event.getX(_activePenButton) / _drawSizeX,
				event.getY(_activePenButton) / _drawSizeY,
				event.getX(_activePenButton), event.getY(_activePenButton),
				event.getPressure(_activePenButton), System.currentTimeMillis());
	}
	
	public void onTouchEvent(final MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			penLevelEvent(event);
			break;
		
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_DOWN:
			penButtonEvent(event);
			break;
			
		case MotionEvent.ACTION_HOVER_ENTER:
		case MotionEvent.ACTION_HOVER_EXIT:
			penKindEvent(event);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private int getButtonState(final MotionEvent e) {
		return e.getButtonState();
	}

	private void penButtonEvent(final MotionEvent e) {
		// System.out.println("Button: " + e.button.getType().toString() +
		// " -> " + e.button.value);
		// override active tool
		boolean activate = false;
		boolean inverted = false;
		// dispatch event
		switch (_penKind) {
		case MotionEvent.TOOL_TYPE_STYLUS:
		case MotionEvent.TOOL_TYPE_FINGER:
		case MotionEvent.TOOL_TYPE_UNKNOWN:
		case MotionEvent.TOOL_TYPE_MOUSE:
			activate = true;
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				if (getButtonState(e) != 0) {
					inverted = true;
				}
			}
			break;
		case MotionEvent.TOOL_TYPE_ERASER:
			activate = true;
			inverted = true;
			break;
		default:
		}
		if (activate) {
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				// Activate tool
				// At first check for active tool
				if (_activeTool != null) {
					// Deactivate tool and store result
					_activeTool.end();
					_activeTool = null;
				}
				// Use the new tool
				_activePenKind = _penKind;
				_activePenButton = e.getActionIndex();
				_activeTool = inverted ? _invertedTool : _normalTool;
				if (_activeTool != null) {
					// Update hover
					if (_hoverTool != _activeTool) {
						if (_hoverTool != null) {
							_hoverTool.out();
						}
						_hoverTool = _activeTool;
						_hoverTool.over();
					}
					// Activate tool
					_activeTool.begin();
					final DataPoint dp = getDataPoint(e);
					_lastData = null;
					_activeTool.draw(dp);
				}
			} else if (_activePenKind == _penKind
					&& _activePenButton == e.getActionIndex()) {
				// Deactivate tool and store result
				if (_activeTool != null) {
					_activeTool.end();
					if ((_penKind != MotionEvent.TOOL_TYPE_ERASER) && (_activeTool == _invertedTool)) {
						_invertedTool.out();
						_activePenButton = -1;
						_hoverTool = _normalTool;
						_normalTool.over();
					}
					_activeTool = null;
				}
			}
		}
	}

	private void penLevelEvent(final MotionEvent e) {
		if (_activeTool != null) {
			// if there is an active tool
			final DataPoint dp = getDataPoint(e);
			boolean usePoint = true;
			if (_lastData != null) {
				// if it is not the first point, check if we have moved enough
				final float xDiff = dp.getXOrig() - _lastData.getXOrig();
				final float yDiff = dp.getYOrig() - _lastData.getYOrig();
				usePoint = xDiff * xDiff + yDiff * yDiff > 3.0f;
			}
			if (usePoint) {
				_activeTool.draw(dp);
				_lastData = dp;
			}
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private int getToolType(final MotionEvent event) {
		return event.getToolType(0);
	}

	private void penKindEvent(final MotionEvent event) {
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			_penKind = getToolType(event);
		} else {
			_penKind = MotionEvent.TOOL_TYPE_UNKNOWN;
		}
		// System.out.println("Kind: " + e.kind.getType().toString());
		// Only if no tool is active
		if (_activeTool == null) {
			// Clear last hover tool
			if (_hoverTool != null) {
				_hoverTool.out();
				_hoverTool = null;
			}
			switch (_penKind) {
			case MotionEvent.TOOL_TYPE_FINGER:
			case MotionEvent.TOOL_TYPE_MOUSE:
			case MotionEvent.TOOL_TYPE_STYLUS:
			case MotionEvent.TOOL_TYPE_UNKNOWN:
				_hoverTool = _normalTool;
				break;
			case MotionEvent.TOOL_TYPE_ERASER:
				_hoverTool = _invertedTool;
				break;
			default:
				_hoverTool = null;
			}
			// Update new hover tool
			if (_hoverTool != null) {
				_hoverTool.over();
			}
		}
	}

	public ITool getNormalTool() {
		return _normalTool;
	}

	public void setNormalTool(final ITool normalTool) {
		if (_normalTool != null) {
			if (_hoverTool == _normalTool) {
				if (_activeTool == _normalTool) {
					_activeTool.end();
					_activeTool = null;
				}
				_normalTool.out();
				_hoverTool = normalTool;
				_hoverTool.over();
			}
		}
		_normalTool = normalTool;
	}

	public ITool getInvertedTool() {
		return _invertedTool;
	}

	public void setInvertedTool(final ITool invertedTool) {
		if (_invertedTool != null) {
			if (_hoverTool == _invertedTool) {
				if (_activeTool == _invertedTool) {
					_activeTool.end();
					_activeTool = null;
				}
				_hoverTool.out();
				_hoverTool = invertedTool;
				_hoverTool.over();
			}
		}
		_invertedTool = invertedTool;
	}

	public void setDrawSize(final float drawSizeX, final float drawSizeY) {
		System.out.println("Draw size: " + drawSizeX + ", " + drawSizeY);
		_drawSizeX = drawSizeX;
		_drawSizeY = drawSizeY;
	}
	
	/**
	 * Stop the currently active tool
	 */
	public void stopTool() {
		// Deactivate tool and store result
		if (_activeTool != null) {
			_activeTool.end();
			if ((_penKind != MotionEvent.TOOL_TYPE_ERASER) && (_activeTool == _invertedTool)) {
				_invertedTool.out();
				_activePenButton = -1;
				_hoverTool = _normalTool;
				_normalTool.over();
			}
			_activeTool = null;
		}
	}
}
