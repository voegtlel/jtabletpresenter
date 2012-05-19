package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;

public class PageProperties {
	private Color _backgroundColor;

	public PageProperties() {
		_backgroundColor = Color.WHITE;
	}

	public Color getBackgroundColor() {
		return _backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		_backgroundColor = backgroundColor;
	}
}
