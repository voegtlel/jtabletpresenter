/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui.buttons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.gui.JPageToolButton;
import de.freiburg.uni.tablet.presenter.gui.JPageToolFrame;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

/**
 * @author lukas
 * 
 */
public class ButtonColor extends AbstractButtonAction {
	private final JPageToolFrame<Color> _tool;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonColor(final IToolPageEditor editor) {
		super(editor, "Color", "/buttons/color-select.png");
		_tool = new JPageToolFrame<Color>();
		_tool.setLocationByPlatform(false);
		_tool.setSize(JPageToolButton.WIDTH_NORMAL * 2,
				JPageToolButton.HEIGHT_NORMAL * 2);
	}

	@Override
	public void perform(final Component button) {
		final Point loc = button.getLocationOnScreen();
		_tool.setLocation(loc.x + button.getWidth(), loc.y);
		_tool.addValue("/buttons/color-red.png", Color.RED);
		_tool.addValue("/buttons/color-green.png", Color.GREEN);
		_tool.addValue("/buttons/color-blue.png", Color.BLUE);
		_tool.addValue("/buttons/color-black.png", Color.BLACK);
		_tool.setSelectedValue(_editor.getCurrentPen().getColor());
		_tool.setVisible(true);
		final Color selectedColor = _tool.getSelectedValue();
		if (!selectedColor.equals(_editor.getCurrentPen().getColor())) {
			_editor.setCurrentPen(new SolidPen(_editor.getCurrentPen()
					.getThickness(), selectedColor));
		}
	}
}
