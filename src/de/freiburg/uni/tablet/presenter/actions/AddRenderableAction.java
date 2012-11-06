/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

/**
 * @author lukas
 * 
 */
public class AddRenderableAction implements IAction, IBinarySerializable {
	private final DocumentPageLayer _layer;
	private final IRenderable _renderable;

	/**
	 * 
	 */
	public AddRenderableAction(final DocumentPageLayer layer,
			final IRenderable renderable) {
		_layer = layer;
		_renderable = renderable;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddRenderableAction(final BinaryDeserializer reader)
			throws IOException {
		_layer = reader.readObjectTable();
		_renderable = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new RemoveRenderableAction(_layer, _renderable);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		_layer.addRenderable(_renderable);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_layer.getId(), _layer);
		writer.writeObjectTable(_renderable.getId(), _renderable);
	}

}
