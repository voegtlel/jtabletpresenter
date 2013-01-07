/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Color;
import java.awt.Point;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolMenuSelectFrame;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

/**
 * @author lukas
 * 
 */
public class ButtonColor extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JPageToolMenuSelectFrame<Color> _tool;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonColor(final IToolPageEditor editor) {
		super("color", editor, "Color", "/buttons/color-select.png");
		_tool = new JPageToolMenuSelectFrame<Color>();
		_tool.setSize(JPageToolButton.WIDTH_NORMAL * 2,
				JPageToolButton.HEIGHT_NORMAL * 2);
		_tool.addValue("/buttons/color-red.png", Color.RED);
		_tool.addValue("/buttons/color-green.png", Color.GREEN);
		_tool.addValue("/buttons/color-blue.png", Color.BLUE);
		_tool.addValue("/buttons/color-black.png", Color.BLACK);
	}

	@Override
	public void perform(final Point desiredLocation) {
		_tool.setSelectedValue(_editor.getDocumentEditor().getCurrentPen()
				.getColor());
		_tool.showAt(desiredLocation);
		final Color selectedColor = _tool.getSelectedValue();
		if (!selectedColor.equals(_editor.getDocumentEditor().getCurrentPen()
				.getColor())) {
			_editor.getDocumentEditor().setCurrentPen(
					new SolidPen(_editor.getDocumentEditor().getCurrentPen()
							.getThickness(), selectedColor));
		}
	}
}
