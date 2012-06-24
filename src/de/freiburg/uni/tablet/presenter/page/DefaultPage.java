package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.geometry.AbstractRenderable;

public class DefaultPage extends IPage {
	private Color _backgroundColor;
	private int _nextObjectId = 0;

	private final List<AbstractRenderable> _renderables = new LinkedList<AbstractRenderable>();

	public DefaultPage() {
		super();
		_backgroundColor = Color.WHITE;
	}

	public DefaultPage(final BinaryDeserializer reader) throws IOException {
		super(reader);
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final AbstractRenderable newInstance = reader.readSerializableClass();
			_renderables.add(newInstance);
		}
	}

	public Color getBackgroundColor() {
		return _backgroundColor;
	}

	public void setBackgroundColor(final Color backgroundColor) {
		_backgroundColor = backgroundColor;
	}

	@Override
	public void addRenderable(final AbstractRenderable renderable) {
		_renderables.add(renderable);
	}

	@Override
	public void removeRenderable(final AbstractRenderable renderable) {
		_renderables.remove(renderable);
	}

	@Override
	public void render(final IPageBackRenderer renderer) {
		renderer.clear();
		for (final AbstractRenderable renderable : _renderables) {
			renderable.render(renderer);
		}
	}

	@Override
	public void eraseAt(final EraseInfo eraseInfo) {
		for (final AbstractRenderable renderable : _renderables) {
			renderable.eraseAt(eraseInfo);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_renderables.size());
		for (final AbstractRenderable renderable : _renderables) {
			writer.writeSerializableClass(renderable);
		}
	}

	@Override
	public int getNextObjectId() {
		return _nextObjectId++;
	}
}
