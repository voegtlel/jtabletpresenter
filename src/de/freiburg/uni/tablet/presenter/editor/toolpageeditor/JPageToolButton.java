/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.*;

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

	public static final float WIDTH_NORMAL = 1.1f;
	public static final float WIDTH_WIDE = WIDTH_NORMAL * 1.5f;

	public static final float HEIGHT_NORMAL = 1.4f;
	public static final float HEIGHT_NOTEXT = 1.1f;
	public static final float SIZE_FONT = 0.3f;
	public static final float SPACING = 0.1f;

	/**
	 * 
	 */
	public JPageToolButton(final int baseSize, final boolean wideMode, final String text) {
		super();

		final Dimension dim = new Dimension((int)(baseSize * (wideMode?WIDTH_WIDE:WIDTH_NORMAL)),
				(int)(baseSize * (text==null?HEIGHT_NOTEXT:HEIGHT_NORMAL)));

		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
		setBorder(new CompoundBorder(new LineBorder(new Color(0, 0, 0)),
				new EmptyBorder((int)(SPACING * baseSize), (int)(SPACING * baseSize),
						(int)(SPACING * baseSize), (int)(SPACING * baseSize))));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		setMargin(new Insets(0, 0, 0, 0));
		setIconTextGap(0);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setFocusable(false);
		setFont(new Font("Dialog", Font.PLAIN, (int)(baseSize * SIZE_FONT)));
		if (text != null) {
			setText(text);
		}
	}

	public JPageToolButton(final String text, final Image image,
			final int baseSize, final boolean wideMode) {
		this(baseSize, wideMode, text);

		setIcon(new ImageIcon(image));
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
