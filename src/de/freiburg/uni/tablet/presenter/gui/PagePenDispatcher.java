package de.freiburg.uni.tablet.presenter.gui;

import javax.swing.event.EventListenerList;

import jpen.PButton;
import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PKindEvent;
import jpen.PLevel.Type;
import jpen.PLevelEvent;
import jpen.PScrollEvent;
import jpen.event.PenListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
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

	private final EventListenerList _listenerList = new EventListenerList();

	public PagePenDispatcher() {
	}

	public void addListener(final IPenListener l) {
		_listenerList.add(IPenListener.class, l);
	}

	public void removeListener(final IPenListener l) {
		_listenerList.remove(IPenListener.class, l);
	}

	protected void fireEventBegin(final ITool activeTool) {
		PenEvent args = null;
		final Object[] listeners = _listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IPenListener.class) {
				if (args == null) {
					args = new PenEvent(this, activeTool);
				}
				((IPenListener) listeners[i + 1]).begin(args);
			}
		}
	}

	protected void fireEventEnd(final ITool activeTool, final IRenderable result) {
		PenEndEvent args = null;
		final Object[] listeners = _listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IPenListener.class) {
				if (args == null) {
					args = new PenEndEvent(this, activeTool, result);
				}
				((IPenListener) listeners[i + 1]).end(args);
			}
		}
	}

	@Override
	public void penButtonEvent(final PButtonEvent e) {
		System.out.println("Button: " + e.button.getType().toString() + " -> "
				+ e.button.value);
		// override active tool
		boolean activate = false;
		boolean inverted = false;
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
					final IRenderable result = _activeTool.end();
					final ITool activeTool = _activeTool;
					_activeTool = null;
					fireEventEnd(activeTool, result);
				}
				// Use the new tool
				_activePenKind = _penKind;
				_activePenButton = e.button.getType();
				_activeTool = (inverted ? _invertedTool : _normalTool);
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
					fireEventBegin(_activeTool);
					final DataPoint dp = new DataPoint(
							e.pen.getLevelValue(Type.X),
							e.pen.getLevelValue(Type.Y),
							e.pen.getLevelValue(Type.X),
							e.pen.getLevelValue(Type.Y),
							e.pen.getLevelValue(Type.PRESSURE), e.getTime());
					_lastData = null;
					_activeTool.draw(dp);
				}
			} else if ((_activePenKind == _penKind)
					&& (_activePenButton == e.button.getType())) {
				// Deactivate tool and store result
				if (_activeTool != null) {
					final IRenderable result = _activeTool.end();
					final ITool activeTool = _activeTool;
					_activeTool = null;
					fireEventEnd(activeTool, result);
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
			final DataPoint dp = new DataPoint(e.pen.getLevelValue(Type.X),
					e.pen.getLevelValue(Type.Y), e.pen.getLevelValue(Type.X),
					e.pen.getLevelValue(Type.Y),
					e.pen.getLevelValue(Type.PRESSURE), e.getTime());
			boolean usePoint = true;
			if (_lastData != null) {
				final float xDiff = dp.getXOrig() - _lastData.getXOrig();
				final float yDiff = dp.getYOrig() - _lastData.getYOrig();
				usePoint = (((xDiff * xDiff) + (yDiff * yDiff)) > 3.0f);
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
		System.out.println("Kind: " + e.kind.getType().toString());
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
		System.out.println("Scroll: " + e.scroll.getType().toString());
	}

	@Override
	public void penTock(final long availableMillis) {
		if (availableMillis <= 0) {
			System.err.println("Warning: Too slow");
		}
	}

	public ITool getNormalTool() {
		return _normalTool;
	}

	public void setNormalTool(final ITool normalTool) {
		_normalTool = normalTool;
	}

	public ITool getInvertedTool() {
		return _invertedTool;
	}

	public void setInvertedTool(final ITool invertedTool) {
		_invertedTool = invertedTool;
	}
}
