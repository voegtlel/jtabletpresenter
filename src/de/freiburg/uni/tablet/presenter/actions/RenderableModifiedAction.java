/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

/**
 * @author lukas
 * 
 */
public class RenderableModifiedAction implements IAction {
	private final IRenderable _renderableInstance;

	/**
	 * 
	 */
	public RenderableModifiedAction(final IRenderable renderable) {
		_renderableInstance = renderable;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public RenderableModifiedAction(final BinaryDeserializer reader)
			throws IOException {
		_renderableInstance = reader.readObjectTable();
		_renderableInstance.deserializeData(reader);
	}

	@Override
	public boolean hasUndoAction() {
		return false;
	}

	@Override
	public IAction getUndoAction() {
		return null;
	}

	@Override
	public void perform(final DocumentEditor editor) {
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_renderableInstance);
		_renderableInstance.serializeData(writer);
	}
}
