/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Color;
import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

/**
 * @author lukas
 * 
 */
public class ButtonColorItem extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Color _color;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonColorItem(final IToolPageEditor editor, final String name, final String text, final String imageResource, final Color color) {
		super(name, editor, text, imageResource);
		_color = color;
	}

	@Override
	public void performLater(final Component component) {
		if (!_color.equals(_editor.getDocumentEditor().getCurrentPen()
				.getColor())) {
			_editor.getDocumentEditor().setCurrentPen(
					new SolidPen(_editor.getDocumentEditor().getCurrentPen()
							.getThickness(), _color));
		}
	}

	public Color getColor() {
		return _color;
	}
}
