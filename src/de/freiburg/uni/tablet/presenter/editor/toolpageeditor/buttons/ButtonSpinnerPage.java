/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditorListener;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;

/**
 * @author lukas
 * 
 */
public class ButtonSpinnerPage extends AbstractButtonAction {
	private final JSpinner _spinner;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonSpinnerPage(final IToolPageEditor editor) {
		super(editor, null, null);
		_spinner = new JSpinner();
		_spinner.setMinimumSize(new Dimension(JPageToolButton.WIDTH_NORMAL, 0));
		_spinner.setMaximumSize(new Dimension(JPageToolButton.WIDTH_NORMAL,
				Integer.MAX_VALUE));
		_spinner.setPreferredSize(new Dimension(JPageToolButton.WIDTH_NORMAL,
				_spinner.getPreferredSize().height));
		_spinner.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		_spinner.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				onSpinnnerChanged();
			}
		});
		_editor.addListener(new IToolPageEditorListener() {
			@Override
			public void pageNumberChanged() {
				if (!_spinner.getValue().equals(_editor.getPageIndex())) {
					_spinner.setValue(_editor.getPageIndex());
				}
			}
		});
	}

	/**
	 * Event.
	 */
	protected void onSpinnnerChanged() {
		_editor.setPageIndex((Integer) _spinner.getValue());
	}

	@Override
	public Component getControl() {
		return _spinner;
	}
}
