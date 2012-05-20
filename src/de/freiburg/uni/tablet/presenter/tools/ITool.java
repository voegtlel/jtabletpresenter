package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface ITool {
	void begin();

	void draw(DataPoint data);

	IRenderable end();

	void over();

	void out();
}
