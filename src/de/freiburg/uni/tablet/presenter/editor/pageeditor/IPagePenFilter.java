package de.freiburg.uni.tablet.presenter.editor.pageeditor;

public interface IPagePenFilter {

	/**
	 * Processes the down event. If it returns false, then the event was consumed.
	 * @param x
	 * @param y
	 * @param pressure
	 * @param penKind
	 * @return
	 */
	boolean onDown(float x, float y, float pressure, jpen.PKind.Type penKind);
	
	/**
	 * Processes the up event. If it returns false, then the event was consumed.
	 * 
	 * @param x
	 * @param y
	 * @param pressure
	 * @param penKind
	 * @return
	 */
	boolean onUp(float x, float y, float pressure, jpen.PKind.Type penKind);

	/**
	 * Processes the move event. If it returns false, then the event was consumed.
	 * 
	 * @param x
	 * @param y
	 * @param pressure
	 * @param penKind
	 * @return
	 */
	boolean onMove(float x, float y, float pressure,
			jpen.PKind.Type penKind);

}
