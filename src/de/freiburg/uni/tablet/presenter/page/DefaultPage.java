package de.freiburg.uni.tablet.presenter.page;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public class DefaultPage implements IPage {
	private Color _backgroundColor;

	private final List<IRenderable> _renderables = new LinkedList<IRenderable>();

	public DefaultPage() {
		_backgroundColor = Color.WHITE;
	}

	public DefaultPage(final DataInputStream reader) throws IOException {
		final int count = reader.readInt();
		try {
			for (int i = 0; i < count; i++) {
				final String className = reader.readUTF();
				final Class<?> c = Class.forName(className);
				final Constructor<?> constructor = c
						.getConstructor(DataInputStream.class);
				final IRenderable newInstance = (IRenderable) constructor
						.newInstance(reader);
				_renderables.add(newInstance);
			}
		} catch (final ClassNotFoundException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final SecurityException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final NoSuchMethodException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final InstantiationException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final IllegalAccessException e) {
			throw new IOException("Invalid format: " + e);
		} catch (final InvocationTargetException e) {
			throw new IOException("Invalid format: " + e);
		}
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
	public void eraseAt(final EraseInfo eraseInfo) {
		for (final IRenderable renderable : _renderables) {
			renderable.eraseAt(eraseInfo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.freiburg.uni.tablet.presenter.IBinarySerializable#serialize(java.io
	 * .DataOutputStream)
	 */
	@Override
	public void serialize(final DataOutputStream writer) throws IOException {
		writer.writeInt(_renderables.size());
		for (final IRenderable renderable : _renderables) {
			writer.writeUTF(renderable.getClass().getName());
			renderable.serialize(writer);
		}
	}
}
