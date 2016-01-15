/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.AbstractButtonAction;

public class JPageToolMenuFrame<T> extends JDialog {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The currently active component
	 */
	private Component _activeComponent = null;
	
	private List<AbstractButtonAction> _actions = new ArrayList<AbstractButtonAction>();
	
	/**
	 * Create the frame.
	 */
	public JPageToolMenuFrame() {
		super((Dialog) null, ModalityType.DOCUMENT_MODAL);
		setUndecorated(true);
		setResizable(false);
		setAlwaysOnTop(true);
		setLocationByPlatform(false);
		setBounds(new Rectangle(0, 0, 75, 200));
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				if (JPageToolMenuFrame.this._activeComponent == null) {
					JPageToolMenuFrame.this.setVisible(false);
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					JPageToolMenuFrame.this.setVisible(false);
				}
			}
		});
	}
	
	/**
	 * Adds a subitem
	 */
	public void addItem(final AbstractButtonAction buttonAction, final int baseSize, final boolean wideMode) {
		if (buttonAction.getControl() != null) {
			getContentPane().add(buttonAction.getControl());
		} else {
			final JButton button = new JPageToolButton(
					buttonAction.getText(),
					buttonAction.getImageResource(baseSize, baseSize),
					baseSize, wideMode);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					JPageToolMenuFrame.this.onActionPerformed(button, buttonAction);
				}
			});
			getContentPane().add(button);
		}
		_actions.add(buttonAction);
	}
	
	/**
	 * Adds a subitem as dummy item (not clickable, but configable)
	 */
	public void addItemDummy(final AbstractButtonAction buttonAction) {
		_actions.add(buttonAction);
	}
	
	private void onActionPerformed(final JButton button, final IButtonAction action) {
		if (_activeComponent == null) {
			_activeComponent = button;
			for (Component c : getContentPane().getComponents()) {
				if (c != button) {
					c.setEnabled(false);
				}
			}
			JPageToolMenuFrame.this.setEnabled(false);
			final Point loc = JPageToolMenuFrame.this.getLocationOnScreen();
			loc.x += JPageToolMenuFrame.this.getWidth();
			action.perform(loc);
			JPageToolMenuFrame.this.setVisible(false);
			_activeComponent = null;
		} else {
			this.toFront();
		}
	}
	
	/**
	 * Gets all actions
	 * @return
	 */
	public List<AbstractButtonAction> getActions() {
		return Collections.unmodifiableList(_actions);
	}

	/**
	 * Moves the component to the position and makes it visible.
	 * 
	 * @param location
	 */
	public void showAt(final Point location) {
		_activeComponent = null;
		JPageToolMenuFrame.this.setEnabled(true);
		for (Component c : getContentPane().getComponents()) {
			c.setEnabled(true);
		}
		setLocation(location.x, location.y);
		setVisible(true);
	}
	
	@Override
	public void dispose() {
		for (AbstractButtonAction buttonAction : _actions) {
			buttonAction.dispose();
		}
		super.dispose();
	}
}
