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
public class DeleteRenderableAction implements IAction, IBinarySerializable {
	private final long _layerId;
	private final long _renderableId;

	/**
	 * 
	 */
	public DeleteRenderableAction(final long layerId, final long renderableId) {
		_layerId = layerId;
		_renderableId = renderableId;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public DeleteRenderableAction(final BinaryDeserializer reader)
			throws IOException {
		_layerId = reader.readLong();
		_renderableId = reader.readLong();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new AddRenderableAction(_layerId, _renderableId);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		final DocumentPageLayer layer = (DocumentPageLayer) editor
				.getDocument().getObject(_layerId);
		if (layer == null) {
			throw new IllegalStateException("Layer with id " + _layerId
					+ " doesn't exist");
		}
		final IRenderable renderable = (IRenderable) editor.getDocument()
				.getObject(_renderableId);
		layer.addRenderable(renderable);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_layerId);
		writer.writeLong(_renderableId);
	}

}
