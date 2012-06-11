package de.freiburg.uni.tablet.presenter.editor.gui;

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
	void end(PenEndEvent e);

}
