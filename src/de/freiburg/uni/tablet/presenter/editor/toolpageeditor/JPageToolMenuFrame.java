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

import javax.swing.JButton;
import javax.swing.JDialog;

import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.AbstractButtonAction;

public class JPageToolMenuFrame<T> extends JDialog {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;
	
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
				if(!e.isTemporary()) {
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
					action.perform(JPageToolMenuFrame.this);
					JPageToolMenuFrame.this.setVisible(false);
				}
			});
			getContentPane().add(button);
		}
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
		final Point loc = relativeComponent.getLocationOnScreen();
		setLocation(loc.x + xOffset, loc.y + yOffset);
		setVisible(true);
	}
}
