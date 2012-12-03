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
public class RemoveRenderableAction extends AbstractAction implements IAction, IBinarySerializable {
	private final DocumentPageLayer _layer;
	private final IRenderable _renderable;

	/**
	 * 
	 */
	public RemoveRenderableAction(int clientId, final DocumentPageLayer layer,
			final IRenderable renderable) {
		super(clientId);
		_layer = layer;
		_renderable = renderable;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public RemoveRenderableAction(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
		_layer = reader.readObjectTable();
		_renderable = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction(int clientId) {
		return new AddRenderableAction(clientId, _layer, _renderable);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		_layer.removeRenderable(_renderable);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_layer.getId(), _layer);
		writer.writeObjectTable(_renderable.getId(), _renderable);
	}

}
