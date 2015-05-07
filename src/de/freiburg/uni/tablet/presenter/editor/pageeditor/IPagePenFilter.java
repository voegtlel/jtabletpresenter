package de.freiburg.uni.tablet.presenter.editor.pageeditor;

public interface IPagePenFilter {

	boolean onDown(float x, float y, float pressure, jpen.PKind.Type penKind);
	
	boolean onUp(float x, float y, float pressure, jpen.PKind.Type penKind);

	boolean onMove(float x, float y, float pressure,
			jpen.PKind.Type penKind);

}
