package de.freiburg.uni.tablet.presenter.page;

import java.util.EventListener;

public interface RedrawListener extends EventListener {

	void redraw(RedrawEvent args);

}
