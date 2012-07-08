/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageToolButton;

/**
 * @author lukas
 * 
 */
public class ButtonSpinnerPage extends AbstractButtonAction {
	private final JSpinner _spinner;
	private final DocumentEditorAdapter _documentEditorListener;

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
		_documentEditorListener = new DocumentEditorAdapter() {

			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage) {
				onUpdateSpinner();
			}
		};
		_editor.getDocumentEditor().addListener(_documentEditorListener);
	}

	protected void onUpdateSpinner() {
		final int pageIndex = _editor.getDocumentEditor().getCurrentPageIndex();
		if (!_spinner.getValue().equals(pageIndex)) {
			_spinner.setValue(pageIndex);
		}
	}

	/**
	 * Event.
	 */
	protected void onSpinnnerChanged() {
		_editor.getDocumentEditor().setCurrentPageByIndex(
				(Integer) _spinner.getValue(), true);
	}

	@Override
	public void onUpdateEditor(final DocumentEditor lastEditor) {
		lastEditor.removeListener(_documentEditorListener);
		_editor.getDocumentEditor().addListener(_documentEditorListener);
	}

	@Override
	public Component getControl() {
		return _spinner;
	}
}
