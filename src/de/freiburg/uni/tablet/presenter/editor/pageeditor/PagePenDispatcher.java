package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.geom.Point2D;

import jpen.PButton;
import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PKindEvent;
import jpen.PLevel.Type;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.Pen;
import jpen.event.PenListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.tools.ITool;

public class PagePenDispatcher implements PenListener {
	private ITool _normalTool;
	private ITool _invertedTool;

	private PKind.Type _activePenKind;
	private PButton.Type _activePenButton;
	private ITool _activeTool;

	private ITool _hoverTool;

	private PKind.Type _penKind = PKind.Type.CURSOR;

	private DataPoint _lastData = null;

	private Point2D.Float _drawSize = new Point2D.Float();
	private Point2D.Float _drawOffset = new Point2D.Float();

	private long _frameReduction = 20;
	private boolean _lockPressure = false;
	private float _minPressure = 0;
	
	private IPagePenFilter _filter = null;
	
	private Object _synch = new Object();

	public PagePenDispatcher() {
	}

	/**
	 * Reads the data point
	 * 
	 * @param pen
	 * @param timestamp
	 * @return
	 */
	private DataPoint getDataPoint(final Pen pen, final float pressure, final long timestamp) {
		return new DataPoint((pen.getLevelValue(Type.X) - _drawOffset.x) / _drawSize.x,
				(pen.getLevelValue(Type.Y) - _drawOffset.y) / _drawSize.y,
				pen.getLevelValue(Type.X), pen.getLevelValue(Type.Y),
				_lockPressure?0.5f:Math.max(_minPressure, pressure), timestamp);
	}

	@Override
	public void penButtonEvent(final PButtonEvent e) {
		// System.out.println("Button: " + e.button.getType().toString() +
		// " -> " + e.button.value);
		// override active tool
		boolean activate = false;
		boolean inverted = false;
		boolean setDefaultPressure = false;
		// dispatch event
		switch (_penKind) {
		case CURSOR:
			switch (e.button.getType()) {
			case LEFT:
				activate = true;
				setDefaultPressure = true;
				break;
			case RIGHT:
				activate = true;
				inverted = true;
				setDefaultPressure = true;
				break;
			default:
			}
			break;
		case STYLUS:
			switch (e.button.getType()) {
			case ON_PRESSURE:
				activate = true;
				break;
			default:
			}
			break;
		case ERASER:
			switch (e.button.getType()) {
			case ON_PRESSURE:
				activate = true;
				inverted = true;
				break;
			default:
			}
			break;
		default:
		}
		synchronized (_synch) {
			if (activate) {
				if (e.button.value) {
					// Activate tool
					// At first check for active tool
					if (_activeTool != null) {
						// Deactivate tool and store result
						_activeTool.end();
						_activeTool = null;
					}
					// Use the new tool
					_activePenKind = _penKind;
					_activePenButton = e.button.getType();
					float pressure = (setDefaultPressure?IPen.DEFAULT_PRESSURE:e.pen.getLevelValue(Type.PRESSURE));
					if (_filter != null && !_filter.onDown(e.pen.getLevelValue(Type.X), e.pen.getLevelValue(Type.Y), pressure, _penKind)) {
						return;
					}
					_activeTool = inverted ? _invertedTool : _normalTool;
					if (_activeTool != null) {
						// Update hover
						if (_hoverTool != _activeTool) {
							if (_hoverTool != null) {
								_hoverTool.out();
							}
							_hoverTool = _activeTool;
							if (_hoverTool != null) {
								_hoverTool.over();
							}
						}
						// Activate tool
						final DataPoint dp = getDataPoint(e.pen, pressure, e.getTime());
						_activeTool.begin();
						_lastData = null;
						_activeTool.draw(dp);
					}
				} else if (_activePenKind == _penKind
						&& _activePenButton == e.button.getType()) {
					// Deactivate tool and store result
					if (_activeTool != null) {
						_activeTool.end();
						if ((_penKind == PKind.Type.CURSOR) && (_activeTool == _invertedTool)) {
							_invertedTool.out();
							_activePenButton = PButton.Type.LEFT;
							_hoverTool = _normalTool;
							if (_hoverTool != null) {
								_hoverTool.over();
							}
						}
						_activeTool = null;
					} else if (_filter != null && !_filter.onUp(e.pen.getLevelValue(Type.X), e.pen.getLevelValue(Type.Y), e.pen.getLevelValue(Type.PRESSURE), _penKind)) {
						return;
					}
				}
			}
		}
	}

	@Override
	public void penLevelEvent(final PLevelEvent e) {
		if (!e.isMovement()) {
			return;
		}
		final DataPoint dp = getDataPoint(e.pen, e.pen.getLevelValue(Type.PRESSURE), e.getTime());
		boolean usePoint = true;
		synchronized (_synch) {
			if (_lastData != null) {
				// if it is not the first point, check if we have moved enough
				final float xDiff = dp.getXOrig() - _lastData.getXOrig();
				final float yDiff = dp.getYOrig() - _lastData.getYOrig();
				usePoint = xDiff * xDiff + yDiff * yDiff > 3.0f;
			}
			if (usePoint) {
				if (_activeTool != null) {
					_activeTool.draw(dp);
					_lastData = dp;
				} else if (_filter != null && !_filter.onMove(e.pen.getLevelValue(Type.X), e.pen.getLevelValue(Type.Y), e.pen.getLevelValue(Type.PRESSURE), _penKind)) {
					return;
				}
				_invertedTool.drawAlways(dp);
				_normalTool.drawAlways(dp);
			}
		}
	}

	@Override
	public void penKindEvent(final PKindEvent e) {
		synchronized (_synch) {
			_penKind = e.kind.getType();
			// System.out.println("Kind: " + e.kind.getType().toString());
			// Only if no tool is active
			if (_activeTool == null) {
				// Clear last hover tool
				if (_hoverTool != null) {
					_hoverTool.out();
					_hoverTool = null;
				}
				switch (_penKind) {
				case CURSOR:
					_hoverTool = _normalTool;
					break;
				case STYLUS:
					_hoverTool = _normalTool;
					break;
				case ERASER:
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
	}

	@Override
	public void penScrollEvent(final PScrollEvent e) {
		// System.out.println("Scroll: " + e.scroll.getType().toString());
	}

	@Override
	public void penTock(final long availableMillis) {
		synchronized (_synch) {
			if (_activeTool != null) {
				if (availableMillis <= 0) {
					_frameReduction = (_frameReduction - availableMillis);
					System.err.println("Warning: Too slow, reduce to "
							+ _frameReduction + " (" + availableMillis + ")");
				} else if (_frameReduction > 0) {
					_frameReduction = (_frameReduction - 1) * 2 / 3;
					if (_frameReduction < 0) {
						_frameReduction = 0;
					}
					System.out.println("Increase FPS to " + _frameReduction + " ("
							+ availableMillis + ")");
				}
			}
		}
	}

	public ITool getNormalTool() {
		return _normalTool;
	}

	public void setNormalTool(final ITool normalTool) {
		synchronized (_synch) {
			if (_normalTool != null) {
				if (_hoverTool == _normalTool) {
					if (_activeTool == _normalTool) {
						_activeTool.end();
						_activeTool = null;
					}
					_normalTool.out();
					_hoverTool = normalTool;
					if (_hoverTool != null) {
						_hoverTool.over();
					}
				}
			}
			_normalTool = normalTool;
		}
	}

	public ITool getInvertedTool() {
		return _invertedTool;
	}

	public void setInvertedTool(final ITool invertedTool) {
		synchronized (_synch) {
			if (_invertedTool != null) {
				if (_hoverTool == _invertedTool) {
					if (_activeTool == _invertedTool) {
						_activeTool.end();
						_activeTool = null;
					}
					_hoverTool.out();
					_hoverTool = invertedTool;
					if (_hoverTool != null) {
						_hoverTool.over();
					}
				}
			}
			_invertedTool = invertedTool;
		}
	}

	public void setTransform(final float drawSizeX, final float drawSizeY, final float drawOffsetX, final float drawOffsetY) {
		synchronized (_synch) {
			_drawSize.x = drawSizeX;
			_drawSize.y = drawSizeY;
			_drawOffset.x = drawOffsetX;
			_drawOffset.y = drawOffsetY;
		}
	}

	public long getFrameReduction() {
		return _frameReduction;
	}
	
	/**
	 * Stop the currently active tool
	 */
	public void stopTool() {
		// Deactivate tool and store result
		synchronized (_synch) {
			if (_activeTool != null) {
				_activeTool.end();
				if ((_penKind == PKind.Type.CURSOR) && (_activeTool == _invertedTool)) {
					_invertedTool.out();
					_activePenButton = PButton.Type.LEFT;
					_hoverTool = _normalTool;
					if (_normalTool != null) {
						_normalTool.over();
					}
				}
				_activeTool = null;
			}
		}
	}

	/**
	 * Locks the pressure to a constant level
	 * @param lockPressure
	 */
	public void setLockPressure(final boolean lockPressure) {
		_lockPressure  = lockPressure;
	}
	
	/**
	 * Sets a filter, which can filter events
	 * @param filter
	 */
	public void setFilter(final IPagePenFilter filter) {
		_filter = filter;
	}
}
