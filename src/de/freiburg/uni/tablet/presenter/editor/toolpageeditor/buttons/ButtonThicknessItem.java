/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

/**
 * @author lukas
 * 
 */
public class ButtonThicknessItem extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private float _thickness;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonThicknessItem(final IToolPageEditor editor, final String name, final String text, final String imageResource, final float thickness) {
		super(name, editor, text, imageResource);
		_thickness = thickness;
	}

	@Override
	public void performLater(final Component component) {
		if (Math.abs(_thickness - _editor.getDocumentEditor().getCurrentPen().getThickness()) > 0.01f) {
			_editor.getDocumentEditor().setCurrentPen(
					new SolidPen(_thickness, _editor.getDocumentEditor().getCurrentPen().getColor()));
		}
	}

	public float getThickness() {
		return _thickness;
	}
}
