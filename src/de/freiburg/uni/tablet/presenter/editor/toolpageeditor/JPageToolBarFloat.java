package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JPageToolBarFloat extends JDialog {
	public static final int ORIENTATION_NONE = 0x00;
	public static final int ORIENTATION_LEFT = 0x01;
	public static final int ORIENTATION_RIGHT = 0x02;
	public static final int ORIENTATION_TOP = 0x03;
	public static final int ORIENTATION_BOTTOM = 0x04;
	
	private static final HashMap<String, Integer> ORIENTATION_MAP = new HashMap<>();
	
	static {
		ORIENTATION_MAP.put("NONE", ORIENTATION_NONE);
		ORIENTATION_MAP.put("LEFT", ORIENTATION_LEFT);
		ORIENTATION_MAP.put("RIGHT", ORIENTATION_RIGHT);
		ORIENTATION_MAP.put("TOP", ORIENTATION_TOP);
		ORIENTATION_MAP.put("BOTTOM", ORIENTATION_BOTTOM);
	}
	
	private static final long serialVersionUID = 1L;
	private JPageToolBar _toolBar;
	
	private int _compactSize;
	private int _fullSize;
	private int _currentSize;
	private int _orientation;
	private Component _boundComponent;
	private float _compactOpacity;

	/**
	 * Create the frame.
	 */
	public JPageToolBarFloat(final JFrame owner, final Component boundComponent, final int orientation, final int compactSize, final float compactOpacity) {
		super(owner);
		_boundComponent = boundComponent;
		_orientation = orientation;
		_compactSize = compactSize;
		_compactOpacity = compactOpacity;
		setUndecorated(true);
		_toolBar = new JPageToolBar();
		setContentPane(_toolBar);
		setFocusable(false);
		setFocusableWindowState(false);
		owner.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				updateLocation();
			}
			
			@Override
			public void componentResized(final ComponentEvent e) {
				updateBounds();
			}
			
			@Override
			public void componentShown(final ComponentEvent e) {
				JPageToolBarFloat.this.setVisible(true);
			}
			
			@Override
			public void componentHidden(final ComponentEvent e) {
				JPageToolBarFloat.this.setVisible(false);
			}
		});
		setBounds(0, 0, 0, 0);
		setOpacity(_compactOpacity);
		
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
	        @Override
	        public void eventDispatched(final AWTEvent event) {
	            Object source = event.getSource();
	            if (source instanceof Component) {
	                Component comp = (Component) source;
	                if (event.getID() == MouseEvent.MOUSE_ENTERED) {
		                if (SwingUtilities.isDescendingFrom(comp, JPageToolBarFloat.this)) {
		                	onMouseEntered();
		                }
	                } else if (event.getID() == MouseEvent.MOUSE_EXITED && event instanceof MouseEvent) {
	                	MouseEvent e = (MouseEvent) event;
		                Point p = SwingUtilities.convertPoint(
		                        comp,
		                        e.getPoint(),
		                        JPageToolBarFloat.this);
		                if (SwingUtilities.isDescendingFrom(comp, JPageToolBarFloat.this) && !JPageToolBarFloat.this.contains(p)) {
		                	onMouseExited();
		                }
	                }
	            }
	        }
	    }, MouseEvent.MOUSE_EVENT_MASK);
	}
	
	protected void updateLocation() {
		if (_boundComponent.isShowing()) {
			Point location = _boundComponent.getLocationOnScreen();
			if (_orientation == ORIENTATION_LEFT || _orientation == ORIENTATION_TOP) {
				JPageToolBarFloat.this.setLocation(location);
			} else if (_orientation == ORIENTATION_BOTTOM) {
				location.move(0, _boundComponent.getHeight() - _currentSize);
				JPageToolBarFloat.this.setLocation(location);
			} else if (_orientation == ORIENTATION_RIGHT) {
				location.move(_boundComponent.getWidth() - _currentSize, 0);
				JPageToolBarFloat.this.setLocation(location);
			}
		}
	}
	
	protected void updateBounds() {
		if (_boundComponent.isShowing()) {
			Dimension size = _boundComponent.getSize();
			Point location = _boundComponent.getLocationOnScreen();
			
			if (_orientation == ORIENTATION_LEFT || _orientation == ORIENTATION_TOP) {
				JPageToolBarFloat.this.setLocation(location);
				if (_orientation == ORIENTATION_LEFT) {
					JPageToolBarFloat.this.setSize(_currentSize, size.height);
				} else {
					JPageToolBarFloat.this.setSize(size.width, _currentSize);
				}
			} else if (_orientation == ORIENTATION_BOTTOM) {
				location.move(0, size.height - _currentSize);
				JPageToolBarFloat.this.setLocation(location);
				JPageToolBarFloat.this.setSize(size.width, _currentSize);
			} else if (_orientation == ORIENTATION_RIGHT) {
				location.move(size.width - _currentSize, 0);
				JPageToolBarFloat.this.setLocation(location);
				JPageToolBarFloat.this.setSize(_currentSize, size.height);
			}
		}
	}
	
	protected void onMouseEntered() {
		_currentSize = _fullSize;
		updateBounds();
		setOpacity(1.0f);
	}
	
	protected void onMouseExited() {
		_currentSize = _compactSize;
		updateBounds();
		setOpacity(_compactOpacity);
	}

	public void setActions(final IButtonAction[] toolbarActions) {
		if (_orientation == ORIENTATION_LEFT || _orientation == ORIENTATION_RIGHT) {
			_toolBar.setToolButtonsVertical(toolbarActions);
			_fullSize = _toolBar.getPreferredSize().width;
		} else {
			_toolBar.setToolButtonsHorizontal(toolbarActions);
			_fullSize = _toolBar.getPreferredSize().height;
		}
		_currentSize = _compactSize;
		updateBounds();
		pack();
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
}
