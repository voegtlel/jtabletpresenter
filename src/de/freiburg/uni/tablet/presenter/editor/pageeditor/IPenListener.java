package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import java.util.EventListener;

public interface IPenListener extends EventListener {

	/**
	 * Fired, when the dispatcher starts a new painting
	 * 
	 * @param e
	 *            event arguments
	 */
	void begin(PenEvent e);

	/**
	 * Fired, when the dispatcher finishes a painting
	 * 
	 * @param e
	 *            event arguments
	 */
	void end(PenEvent e);

}
