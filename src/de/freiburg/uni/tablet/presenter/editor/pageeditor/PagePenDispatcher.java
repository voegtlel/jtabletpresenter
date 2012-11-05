package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.awt.Dimension;

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

	private Dimension _drawSize = new Dimension();

	private long _frameReduction = 20;

	public PagePenDispatcher() {
	}

	/**
	 * Reads the data point
	 * 
	 * @param pen
	 * @param timestamp
	 * @return
	 */
	private DataPoint getDataPoint(final Pen pen, final long timestamp) {
		return new DataPoint(pen.getLevelValue(Type.X) / _drawSize.width,
				pen.getLevelValue(Type.Y) / _drawSize.height,
				pen.getLevelValue(Type.X), pen.getLevelValue(Type.Y),
				pen.getLevelValue(Type.PRESSURE), timestamp);
	}

	@Override
	public void penButtonEvent(final PButtonEvent e) {
		// System.out.println("Button: " + e.button.getType().toString() +
		// " -> " + e.button.value);
		// override active tool
		boolean activate = false;
		boolean inverted = false;
		// dispatch event
		switch (_penKind) {
		case CURSOR:
			switch (e.button.getType()) {
			case LEFT:
				activate = true;
				break;
			case RIGHT:
				activate = true;
				inverted = true;
				break;
			}
			break;
		case STYLUS:
			switch (e.button.getType()) {
			case ON_PRESSURE:
				activate = true;
				break;
			}
			break;
		case ERASER:
			switch (e.button.getType()) {
			case ON_PRESSURE:
				activate = true;
				inverted = true;
				break;
			}
			break;
		}
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
					final DataPoint dp = getDataPoint(e.pen, e.getTime());
					_lastData = null;
					_activeTool.draw(dp);
				}
			} else if (_activePenKind == _penKind
					&& _activePenButton == e.button.getType()) {
				// Deactivate tool and store result
				if (_activeTool != null) {
					_activeTool.end();
					_activeTool = null;
				}
			}
		}
	}

	@Override
	public void penLevelEvent(final PLevelEvent e) {
		if (!e.isMovement()) {
			return;
		}
		if (_activeTool != null) {
			// if there is an active tool
			final DataPoint dp = getDataPoint(e.pen, e.getTime());
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

	@Override
	public void penKindEvent(final PKindEvent e) {
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
			}
			// Update new hover tool
			if (_hoverTool != null) {
				_hoverTool.over();
			}
		}
	}

	@Override
	public void penScrollEvent(final PScrollEvent e) {
		// System.out.println("Scroll: " + e.scroll.getType().toString());
	}

	@Override
	public void penTock(final long availableMillis) {
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

	public Dimension getDrawSize() {
		return _drawSize;
	}

	public void setDrawSize(final Dimension drawSize) {
		_drawSize = drawSize;
	}

	public long getFrameReduction() {
		return _frameReduction;
	}
}
