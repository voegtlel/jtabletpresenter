package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface ITool {
	void Begin(DataPoint data);

	void Draw(DataPoint data);

	IRenderable End(DataPoint data);

	void Over(DataPoint data);

	void Out(DataPoint data);
}
