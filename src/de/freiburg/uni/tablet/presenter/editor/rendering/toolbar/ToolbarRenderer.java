package de.freiburg.uni.tablet.presenter.editor.rendering.toolbar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import jpen.PKind.Type;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPagePenFilter;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;

public class ToolbarRenderer implements IPagePenFilter {
	public static final int ORIENTATION_NONE = 0x00;
	public static final int ORIENTATION_LEFT = 0x01;
	public static final int ORIENTATION_RIGHT = 0x02;
	public static final int ORIENTATION_TOP = 0x03;
	public static final int ORIENTATION_BOTTOM = 0x04;

	public static final float EMPTY_SPACING = 0.5f;
	public static final float SPACING = 0.05f;
	public static final float PADDING = 0.1f;
	
	private static final HashMap<String, Integer> ORIENTATION_MAP = new HashMap<>();
	
	static {
		ORIENTATION_MAP.put("NONE", ORIENTATION_NONE);
		ORIENTATION_MAP.put("LEFT", ORIENTATION_LEFT);
		ORIENTATION_MAP.put("RIGHT", ORIENTATION_RIGHT);
		ORIENTATION_MAP.put("TOP", ORIENTATION_TOP);
		ORIENTATION_MAP.put("BOTTOM", ORIENTATION_BOTTOM);
	}
	
	/**
	 * Gets the orientation
	 * @param name
	 * @return
	 */
	public static int getOrientation(final String name) {
		Integer orientation = ORIENTATION_MAP.get(name);
		if (orientation == null) {
			throw new IllegalArgumentException("Invalid orientation: " + name);
		}
		return orientation;
	}
	
	private IPageRenderer _renderer;
	private IToolbarItem[] _actions;
	private boolean _enabled;
	
	private Rectangle _bounds = new Rectangle();
	private Rectangle _compactBounds = new Rectangle();
	
	private int _compactScale;
	private int _fullSize;
	private int _orientation;
	private int _actionEmptySpace;
	private int _actionSpacing;
	private int _actionPadding;
	private AlphaComposite _alphaInactive;
	private Font _font;
	private Color _background = new Color(0xd0ffffff, true);
	private Color _backgroundHover = new Color(0xffffffff, true);
	private Color _backgroundDown = new Color(0xfff0f0f0, true);
	private boolean _hoverState;
	private Rectangle _hoverRectangle = new Rectangle();
	private ToolbarAction _hoverAction;
	private ToolbarAction _downAction;
	
	/**
	 * Create the frame.
	 */
	public ToolbarRenderer(final IPageRenderer renderer, final int orientation, final int baseSize, final float compactScale, final float compactOpacity, final Font font, final boolean toolbarEnabled) {
		_renderer = renderer;
		_orientation = orientation;
		_actionEmptySpace = (int)(baseSize * EMPTY_SPACING);
		_actionSpacing = (int)(baseSize * SPACING);
		_actionPadding = (int)(baseSize * PADDING);
		_compactScale = (int)(compactScale * baseSize);
		_font = font;
		_enabled = toolbarEnabled;
		
		_alphaInactive = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, compactOpacity);
	}
	
	public void setActions(final IToolbarItem[] actions) {
		_actions = actions;
		_fullSize = 0;
		int fontOffset = _font.getSize();
		int fontHeight = (int)(fontOffset * 1.25f);
		for (int i = 0; i < _actions.length; i++) {
			if (_actions[i].getType() == ToolbarItemType.Action) {
				ToolbarAction action = (ToolbarAction) _actions[i];
				if (_orientation == ORIENTATION_LEFT || _orientation == ORIENTATION_RIGHT) {
					_fullSize = Math.max(_fullSize, action.getIcon().getIconWidth() + 2 * _actionPadding);
				} else {
					_fullSize = Math.max(_fullSize, action.getIcon().getIconHeight() + fontHeight + 2 * _actionPadding);
				}
			}
		}
	}
	
	private void iterateActions(final IterationListener l) {
		int fontOffset = _font.getSize();
		int fontHeight = (int)(fontOffset * 1.25f);
		
		int pos = 0;
		boolean vertical = (_orientation == ORIENTATION_LEFT || _orientation == ORIENTATION_RIGHT);
		boolean lastWasAction = false;
		for (int i = 0; i < _actions.length; i++) {
			if (_actions[i].getType() == ToolbarItemType.Space) {
				pos += _actionEmptySpace;
				if (vertical) {
					l.iterateSpace(_actions[i], _bounds.x, _bounds.y + pos, _bounds.width, _actionEmptySpace);
				} else {
					l.iterateSpace(_actions[i], _bounds.x + pos, _bounds.y, _actionEmptySpace, _bounds.height);
				}
				lastWasAction = false;
			} else if (_actions[i].getType() == ToolbarItemType.Fill) {
				int requiredSpace = 0;
				int dividers = 1;
				boolean lastCalcWasAction = false;
				for (int j = i + 1; j < _actions.length; j++) {
					if (_actions[j].getType() == ToolbarItemType.Action) {
						if (vertical) {
							requiredSpace += ((ToolbarAction)_actions[j]).getIcon().getIconHeight() + fontHeight;
						} else {
							requiredSpace += ((ToolbarAction)_actions[j]).getIcon().getIconWidth();
						}
						requiredSpace += _actionPadding * 2;
						if (lastCalcWasAction) {
							requiredSpace += _actionSpacing;
						}
						lastCalcWasAction = true;
					} else if (_actions[j].getType() == ToolbarItemType.Space) {
						requiredSpace += _actionEmptySpace;
						lastCalcWasAction = false;
					} else if (_actions[j].getType() == ToolbarItemType.Fill) {
						dividers++;
						lastCalcWasAction = false;
					}
				}
				if (vertical) {
					int width = _bounds.width;
					int height = Math.max(Math.max(_bounds.height - pos - requiredSpace, 0) / dividers, _actionEmptySpace);
					l.iterateFill(_actions[i], _bounds.x, _bounds.y + pos, width, height);
					pos += height;
				} else {
					int width = Math.max(Math.max(_bounds.width - pos - requiredSpace, 0) / dividers, _actionEmptySpace);
					int height = _bounds.height;
					l.iterateFill(_actions[i], _bounds.x + pos, _bounds.y, width, height);
					pos += width;
				}
				lastWasAction = false;
			} else if (_actions[i].getType() == ToolbarItemType.Action) {
				if (lastWasAction) {
					pos += _actionSpacing;
				}
				ToolbarAction action = (ToolbarAction) _actions[i];
				int x;
				int y;
				if (vertical) {
					x = _bounds.x;
					y = _bounds.y + pos;
				} else {
					x = _bounds.x + pos;
					y = _bounds.y;
				}
				
				int buttonHeight = action.getIcon().getIconHeight() + fontHeight + 2 * _actionPadding;
				int buttonWidth = action.getIcon().getIconWidth() + 2 * _actionPadding;
				l.iterateAction(action, x, y, buttonWidth, buttonHeight);
				if (vertical) {
					pos += buttonHeight;
				} else {
					pos += buttonWidth;
				}
				pos += _actionSpacing;
				lastWasAction = true;
			}
		}
	}
	
	public void paint(final Graphics2D graphics) {
		if (!_enabled) {
			return;
		}
		final Rectangle currentBounds = (_hoverState?_bounds:_compactBounds);
		graphics.setClip(currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);
		Composite compositeOrig = graphics.getComposite();
		
		final int offsetX;
		final int offsetY;
		
		if (_hoverState) {
			offsetX = 0;
			offsetY = 0;
		} else {
			graphics.setComposite(_alphaInactive);
			if (_orientation == ORIENTATION_LEFT) {
				offsetX = _compactScale - _fullSize;
				offsetY = 0;
			} else if (_orientation == ORIENTATION_TOP) {
				offsetY = _compactScale - _fullSize;
				offsetX = 0;
			} else {
				offsetX = 0;
				offsetY = 0;
			}
		}
		
		graphics.setFont(_font);
		
		final int fontOffset = _font.getSize();
		
		iterateActions(new IterationListener() {
			@Override
			public void iterateAction(final ToolbarAction action, final int x, final int y, final int width,
					final int height) {
				if (action == _downAction && _downAction == _hoverAction) {
					graphics.setColor(_backgroundDown);
				} else if (action == _hoverAction) {
					graphics.setColor(_backgroundHover);
				} else {
					graphics.setColor(_background);
				}
				graphics.clipRect(x + offsetX, y + offsetY, width, height);
				graphics.fillRect(x + offsetX, y + offsetY, width, height);
				
				graphics.drawImage(action.getIcon().getImage(), x + offsetX + _actionPadding, y + offsetY + _actionPadding, null);
				Rectangle2D bounds = _font.getStringBounds(action.getName(), graphics.getFontRenderContext());
				int offset = Math.max((int)((width - bounds.getWidth()) * 0.5f), 0);
				graphics.setPaint(Color.BLACK);
				graphics.drawString(action.getName(), x + offsetX + offset, y + offsetY + _actionPadding + action.getIcon().getIconHeight() + fontOffset);
				graphics.setClip(currentBounds.x, currentBounds.y, currentBounds.width, currentBounds.height);
			}
		});
		
		
		graphics.setComposite(compositeOrig);
		graphics.setClip(null);
	}
	
	public void updateBounds(final int width, final int height) {
		if (_orientation == ORIENTATION_LEFT) {
			_bounds.setBounds(0, 0, _fullSize, height);
			_compactBounds.setBounds(0, 0, _compactScale, height);
		} else if (_orientation == ORIENTATION_TOP) {
			_bounds.setBounds(0, 0, width, _fullSize);
			_compactBounds.setBounds(0, 0, width, _compactScale);
		} else if (_orientation == ORIENTATION_BOTTOM) {
			_bounds.setBounds(0,  height - _fullSize, width, _fullSize);
			_compactBounds.setBounds(0,  height - _compactScale, width, _compactScale);
		} else if (_orientation == ORIENTATION_RIGHT) {
			_bounds.setBounds(width - _fullSize, 0, _fullSize, height);
			_compactBounds.setBounds(width - _compactScale, 0, _compactScale, height);
		}
	}
	
	public Rectangle getBounds() {
		return _bounds;
	}
	
	public void setEnabled(final boolean enabled) {
		_enabled = enabled;
	}
	
	public boolean isEnabled() {
		return _enabled;
	}
	
	@Override
	public boolean onDown(final float x, final float y, final float pressure, final Type penKind) {
		if (!_enabled) {
			return true;
		}
		_downAction = _hoverAction;
		if (_downAction != null) {
			_renderer.requireRepaint();
			return false;
		} else {
			_hoverState = false;
			_renderer.requireRepaint();
			return true;
		}
	}

	@Override
	public boolean onUp(final float x, final float y, final float pressure, final Type penKind) {
		if (!_enabled) {
			return true;
		}
		if (_downAction == _hoverAction && _downAction != null) {
			Point location = _renderer.getContainerComponent().getLocationOnScreen();
			if (_orientation == ORIENTATION_LEFT) {
				location.x += _hoverRectangle.x + _hoverRectangle.width;
				location.y += _hoverRectangle.y;
			} else if (_orientation == ORIENTATION_RIGHT) {
				location.x += _hoverRectangle.x;
				location.y += _hoverRectangle.y;
			} else if (_orientation == ORIENTATION_TOP) {
				location.x += _hoverRectangle.x;
				location.y += _hoverRectangle.y + _hoverRectangle.height;
			} else if (_orientation == ORIENTATION_BOTTOM) {
				location.x += _hoverRectangle.x;
				location.y += _hoverRectangle.y;
			}
			_downAction.perform(location);
		}
		if (_downAction != null) {
			_downAction = null;
			_renderer.requireRepaint();
			return false;
		}
		return !_hoverState;
	}

	@Override
	public boolean onMove(final float x, final float y, final float pressure, final Type penKind) {
		if (!_enabled) {
			return true;
		}
		Rectangle currentBounds = (_hoverState?_bounds:_compactBounds);
		boolean lastHoverState = _hoverState;
		_hoverState = (x >= currentBounds.x && y >= currentBounds.y && x < currentBounds.getMaxX() && y < currentBounds.getMaxY());
		ToolbarAction lastHoverAction = _hoverAction;
		_hoverAction = null;
		if (_hoverState) {
			iterateActions(new IterationListener() {
				@Override
				public void iterateAction(final ToolbarAction action, final int actionX, final int actionY, final int actionWidth,
						final int actionHeight) {
					if (x >= actionX && y >= actionY && x < actionX + actionWidth && y < actionY + actionHeight) {
						_hoverRectangle.x = actionX;
						_hoverRectangle.y = actionY;
						_hoverRectangle.width = actionWidth;
						_hoverRectangle.height = actionHeight;
						_hoverAction = action;
					}
				}
			});
		}
		if (_hoverAction != lastHoverAction || _hoverState != lastHoverState) {
			_renderer.requireRepaint();
			if (_hoverAction != null) {
				_renderer.setTemporaryCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				_renderer.resetTemporaryCursor();
			}
		}
		return !_hoverState && _downAction != null;
	}
	
	private class IterationListener {

		void iterateSpace(final IToolbarItem space, final int x, final int y, final int width,
				final int height) {}
		void iterateFill(final IToolbarItem fill, final int x, final int y, final int width,
				final int height) {}
		void iterateAction(final ToolbarAction action, final int x, final int y, final int width,
				final int height) {}
		
	}
}
