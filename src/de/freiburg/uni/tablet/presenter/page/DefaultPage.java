package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public class DefaultPage implements IPage {
	private Color _backgroundColor;

	private final List<IRenderable> _renderables = new LinkedList<IRenderable>();

	public DefaultPage() {
		_backgroundColor = Color.WHITE;
	}

	public Color getBackgroundColor() {
		return _backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		_backgroundColor = backgroundColor;
	}

	@Override
	public void addRenderable(final IRenderable renderable) {
		_renderables.add(renderable);
	}

	@Override
	public void removeRenderable(final IRenderable renderable) {
		_renderables.remove(renderable);
	}

	@Override
	public void render(final IPageRenderer renderer) {
		renderer.fill(_backgroundColor);
		for (final IRenderable renderable : _renderables) {
			renderable.render(renderer);
		}
	}

	@Override
	public void eraseAt(final DataPoint data, final float radiusX,
			final float radiusY) {
		// TODO Auto-generated method stub

	}
}
