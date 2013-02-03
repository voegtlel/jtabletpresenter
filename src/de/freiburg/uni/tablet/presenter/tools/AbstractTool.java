package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

public abstract class AbstractTool implements ITool, IPageRepaintListener {
	protected final IToolPageEditor _editor;

	public AbstractTool(final IToolPageEditor editor) {
		_editor = editor;
	}

	@Override
	public void over() {
	}

	@Override
	public void out() {
	}
}
