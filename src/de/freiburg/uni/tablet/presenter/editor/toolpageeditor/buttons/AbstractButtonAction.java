/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonAction extends AbstractAction implements IButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String _name;
	
	protected final IToolPageEditor _editor;
	private final String _text;
	private final String _imageResource;

	/**
	 * Creates the action with an editor.
	 */
	public AbstractButtonAction(final String name, final IToolPageEditor editor,
			final String text, final String imageResource) {
		_name = name;
		_editor = editor;
		_text = text;
		_imageResource = imageResource;
	}
	
	public String getName() {
		return _name;
	}

	@Override
	public String getText() {
		return _text;
	}

	@Override
	public String getImageResource() {
		return _imageResource;
	}

	@Override
	public Component getControl() {
		return null;
	}

	@Override
	public void perform(final Component button) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				performLater(_editor.getPageEditor().getContainerComponent());
			}
		});
	}
	
	public void performLater(final Component component) {
	}

	@Override
	public void onUpdateEditor(final DocumentEditor lastEditor) {
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		perform((Component)e.getSource());
	}
	
	@Override
	public IButtonAction getButton(String name) {
		if (name.equals(this._name)) {
			return this;
		}
		return null;
	}
}
