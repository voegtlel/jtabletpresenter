package de.freiburg.uni.tablet.presenter.tools;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;

public interface ITool {
	void Begin(DataPoint data);

	void Draw(DataPoint data);

	void End(DataPoint data);

	void Over(DataPoint data);

	void Out(DataPoint data);
}
