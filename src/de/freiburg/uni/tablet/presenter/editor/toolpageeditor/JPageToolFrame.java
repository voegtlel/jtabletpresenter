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

import javax.swing.JDialog;

public class JPageToolFrame<T> extends JDialog {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private T _selectedValue;

	/**
	 * Create the frame.
	 */
	public JPageToolFrame() {
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
				JPageToolFrame.this.setVisible(false);
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					JPageToolFrame.this.setVisible(false);
				}
			}
		});
	}

	/**
	 * Adds a tool to the selection list buttons
	 * 
	 * @param name
	 *            display name of the tool
	 * @param resourceImage
	 *            image resource location
	 * @param value
	 *            value
	 */
	public void addValue(final String name, final String resourceImage,
			final T value) {
		final JPageToolButton button = new JPageToolButton(name, resourceImage,
				true);
		getContentPane().add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				_selectedValue = value;
				JPageToolFrame.this.setVisible(false);
			}
		});
	}

	/**
	 * Adds a tool to the selection list buttons
	 * 
	 * @param resourceImage
	 *            image resource location
	 * @param value
	 *            value
	 */
	public void addValue(final String resourceImage, final T value) {
		final JPageToolButton button = new JPageToolButton(resourceImage, false);
		getContentPane().add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				_selectedValue = value;
				JPageToolFrame.this.setVisible(false);
			}
		});
	}

	/**
	 * Creates a JPageToolFrame and shows it modal.
	 * 
	 * @param container
	 *            container for the dialog
	 */
	public static <T> JPageToolFrame<T> createIconBox(
			final Component container, final int buttonWidth,
			final int buttonHeight, final int componentsX, final int componentsY) {
		final JPageToolFrame<T> frame = new JPageToolFrame<T>();
		final Rectangle rect = new Rectangle(container.getLocationOnScreen());
		rect.x += container.getWidth();
		rect.width = buttonWidth * componentsX;
		rect.height = buttonHeight * componentsY;
		frame.setBounds(rect);
		return frame;
	}

	/**
	 * Gets the selected tool
	 * 
	 * @return selected tool
	 */
	public T getSelectedValue() {
		return _selectedValue;
	}

	/**
	 * Sets the selected value
	 * 
	 * @param selectedValue
	 *            new selected value
	 */
	public void setSelectedValue(final T selectedValue) {
		_selectedValue = selectedValue;
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
