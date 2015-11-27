package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Class for an automatic fadable toolbar.
 * @author Lukas
 *
 */
public class JPageToolBar extends JPanel {
	private static final long serialVersionUID = 1L;

	public void setToolButtonsVertical(final IButtonAction[] buttons) {
		// Build layout
		final GridBagLayout gbl_panelTools = new GridBagLayout();
		gbl_panelTools.columnWidths = new int[] { 0 };
		gbl_panelTools.columnWeights = new double[] { 1.0 };
		gbl_panelTools.rowHeights = new int[buttons.length + 1];
		gbl_panelTools.rowWeights = new double[buttons.length + 1];

		for (int i = 0; i < buttons.length; i++) {
			gbl_panelTools.rowWeights[i] = 0.0;
			if (buttons[i] == null) {
				gbl_panelTools.rowHeights[i] = 10;
			} else {
				gbl_panelTools.rowHeights[i] = 0;
			}
		}
		gbl_panelTools.rowWeights[buttons.length] = 1.0;
		gbl_panelTools.rowHeights[buttons.length] = 0;

		setLayout(gbl_panelTools);

		// Add controls
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				final GridBagConstraints gbc_control = new GridBagConstraints();
				gbc_control.insets = new Insets(0, 0, 5, 0);
				gbc_control.fill = GridBagConstraints.BOTH;
				gbc_control.gridx = 0;
				gbc_control.gridy = i;
				if (buttons[i].getControl() != null) {
					add(buttons[i].getControl(), gbc_control);
				} else {
					final IButtonAction action = buttons[i];
					final JButton button = new JPageToolButton(
							buttons[i].getText(),
							buttons[i].getImageResource(), false);
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Point loc = button.getLocationOnScreen();
							loc.x += button.getWidth();
							action.perform(loc);
						}
					});
					add(button, gbc_control);
				}
			}
		}
	}
	
	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,
			final int condition, final boolean pressed) {
		if (e.getSource() != null && e.getSource() instanceof Component) {
			Component src = (Component) e.getSource();
			if (SwingUtilities.isDescendingFrom(src, this) && src != this) {
				System.out.println("event " + e + " ignored");
				return true;
			}
		}
		return super.processKeyBinding(ks, e, condition, pressed);
	}

	public void setToolButtonsHorizontal(final IButtonAction[] buttons) {
		// Build layout
		final GridBagLayout gbl_panelTools = new GridBagLayout();
		gbl_panelTools.rowHeights = new int[] { 0 };
		gbl_panelTools.rowWeights = new double[] { 1.0 };
		gbl_panelTools.columnWidths = new int[buttons.length + 1];
		gbl_panelTools.columnWeights = new double[buttons.length + 1];

		for (int i = 0; i < buttons.length; i++) {
			gbl_panelTools.columnWeights[i] = 0.0;
			if (buttons[i] == null) {
				gbl_panelTools.columnWidths[i] = 10;
			} else {
				gbl_panelTools.columnWidths[i] = 0;
			}
		}
		gbl_panelTools.columnWeights[buttons.length] = 1.0;
		gbl_panelTools.columnWidths[buttons.length] = 0;

		setLayout(gbl_panelTools);

		// Add controls
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				final GridBagConstraints gbc_control = new GridBagConstraints();
				gbc_control.insets = new Insets(0, 0, 0, 5);
				gbc_control.fill = GridBagConstraints.BOTH;
				gbc_control.gridx = i;
				gbc_control.gridy = 0;
				if (buttons[i].getControl() != null) {
					add(buttons[i].getControl(), gbc_control);
				} else {
					final IButtonAction action = buttons[i];
					final JButton button = new JPageToolButton(
							buttons[i].getText(),
							buttons[i].getImageResource(), false);
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Point loc = button.getLocationOnScreen();
							loc.y += button.getHeight();
							action.perform(loc);
						}
					});
					add(button, gbc_control);
				}
			}
		}
	}
}
