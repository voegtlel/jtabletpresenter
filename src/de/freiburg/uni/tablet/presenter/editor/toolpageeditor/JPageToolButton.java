/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class JPageToolButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH_NORMAL = 50;
	public static final int WIDTH_WIDE = 75;
	public static final int HEIGHT_NORMAL = 50;

	/**
	 * 
	 */
	public JPageToolButton(final int width, final int height) {
		super();

		final Dimension dim = new Dimension(width, height);

		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
		setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)),
				new EmptyBorder(5, 5, 5, 5)));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		setMargin(new Insets(0, 0, 0, 0));
		setIconTextGap(0);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setFocusable(false);
		setFont(new Font("Dialog", Font.PLAIN, 12));
	}

	public JPageToolButton(final String text, final String imageResource,
			final boolean wideMode) {
		this((wideMode ? WIDTH_WIDE : WIDTH_NORMAL), HEIGHT_NORMAL);

		setIcon(new ImageIcon(JPageEditor.class.getResource(imageResource)));
		setText(text);
	}

	public JPageToolButton(final String imageResource, final boolean wideMode) {
		this((wideMode ? WIDTH_WIDE : WIDTH_NORMAL), HEIGHT_NORMAL);

		setIcon(new ImageIcon(JPageEditor.class.getResource(imageResource)));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
		} else {
			setBackground(Color.LIGHT_GRAY);
			setForeground(Color.DARK_GRAY);
		}
	}
}
