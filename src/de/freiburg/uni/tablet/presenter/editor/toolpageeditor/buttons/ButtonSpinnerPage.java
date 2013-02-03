/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.content.Context;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonSpinnerPage extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//private final JSpinner _spinner;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonSpinnerPage(final IToolPageEditor editor) {
		super(editor, -1);
		/*_spinner = new JSpinner();
		_spinner.setMinimumSize(new Dimension(JPageToolButton.WIDTH_NORMAL, 0));
		_spinner.setMaximumSize(new Dimension(JPageToolButton.WIDTH_NORMAL,
				Integer.MAX_VALUE));
		_spinner.setPreferredSize(new Dimension(JPageToolButton.WIDTH_NORMAL,
				_spinner.getPreferredSize().height));
		_spinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		_spinner.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				onSpinnnerChanged();
			}
		});
		_editor.getDocumentEditor().addListener(new DocumentEditorAdapter() {
			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage, final DocumentPage lastCurrentBackPage) {
				onUpdateSpinner();
			}
		});*/
	}
	
	@Override
	public void perform(Context context) {
		// TODO Auto-generated method stub
		
	}

	/*
	protected void onUpdateSpinner() {
		final int pageNumber = _editor.getDocumentEditor()
				.getCurrentPageIndex() + 1;
		if (!_spinner.getValue().equals(pageNumber)) {
			_spinner.setValue(pageNumber);
		}
	}

	protected void onSpinnnerChanged() {
		int newPageIndex = (Integer) _spinner.getValue() - 1;
		if (_editor.getConfig().getBoolean("autosave.spinner", true)) {
			FileHelper.autosave(_editor.getDocumentEditor());
		}
		if (newPageIndex >= _editor.getDocumentEditor().getMaxPageCount()) {
			newPageIndex = _editor.getDocumentEditor().getMaxPageCount();
		} else if (newPageIndex < 0) {
			newPageIndex = 0;
		}
		_editor.getDocumentEditor().setCurrentPageByIndex(newPageIndex, true);
	}

	@Override
	public Component getControl() {
		return _spinner;
	}*/
}
