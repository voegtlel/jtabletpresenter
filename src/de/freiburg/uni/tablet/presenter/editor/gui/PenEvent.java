package de.freiburg.uni.tablet.presenter.editor.gui;

import java.util.EventObject;

import de.freiburg.uni.tablet.presenter.tools.ITool;

public class PenEvent extends EventObject {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private final ITool _activeTool;

	public PenEvent(final Object source, final ITool activeTool) {
		super(source);
		_activeTool = activeTool;
	}

	public ITool getActiveTool() {
		return _activeTool;
	}
}
