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
	 * @param buttonAction
	 */
	public void addItem(AbstractButtonAction buttonAction) {
		if (buttonAction.getControl() != null) {
			getContentPane().add(buttonAction.getControl());
		} else {
			final IButtonAction action = buttonAction;
			final JButton button = new JPageToolButton(
					buttonAction.getText(),
					buttonAction.getImageResource(), true);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					JPageToolMenuFrame.this.onActionPerformed(button, action);
				}
			});
			getContentPane().add(button);
		}
		_actions.add(buttonAction);
	}
	
	private void onActionPerformed(JButton button, IButtonAction action) {
		if (_activeComponent == null) {
			_activeComponent = button;
			for (Component c : getContentPane().getComponents()) {
				if (c != button) {
					c.setEnabled(false);
				}
			}
			JPageToolMenuFrame.this.setEnabled(false);
			action.perform(JPageToolMenuFrame.this);
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
	 * Creates a JPageToolFrame and shows it modal.
	 * 
	 * @param container
	 *            container for the dialog
	 */
	public static <T> JPageToolMenuFrame<T> createIconBox(
			final Component container, final int buttonWidth,
			final int buttonHeight, final int componentsX, final int componentsY) {
		final JPageToolMenuFrame<T> frame = new JPageToolMenuFrame<T>();
		final Rectangle rect = new Rectangle(container.getLocationOnScreen());
		rect.x += container.getWidth();
		rect.width = buttonWidth * componentsX;
		rect.height = buttonHeight * componentsY;
		frame.setBounds(rect);
		return frame;
	}

	/**
	 * moves the component to the relative position and makes it visible.
	 * 
	 * @param relativeComponent
	 *            position is relative to this component
	 * @param xOffset
	 *            offset of position
	 * @param yOffset
	 *            offset of position
	 */
	public void showAt(final Component relativeComponent, final int xOffset,
			final int yOffset) {
		_activeComponent = null;
		JPageToolMenuFrame.this.setEnabled(true);
		for (Component c : getContentPane().getComponents()) {
			c.setEnabled(true);
		}
		final Point loc = relativeComponent.getLocationOnScreen();
		setLocation(loc.x + xOffset, loc.y + yOffset);
		setVisible(true);
	}
}
