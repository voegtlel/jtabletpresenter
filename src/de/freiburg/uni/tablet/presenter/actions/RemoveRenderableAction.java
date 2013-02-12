/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

/**
 * @author lukas
 * 
 */
public class RemoveRenderableAction implements IAction {
	private final DocumentPage _page;
	private final IRenderable _afterRenderable;
	private final IRenderable _renderable;

	/**
	 * 
	 */
	public RemoveRenderableAction(final DocumentPage layer,
			final IRenderable afterRenderable, final IRenderable renderable) {
		_page = layer;
		_afterRenderable = afterRenderable;
		_renderable = renderable;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public RemoveRenderableAction(final BinaryDeserializer reader)
			throws IOException {
		_page = reader.readObjectTable();
		_afterRenderable = reader.readObjectTable();
		_renderable = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new AddRenderableAction(_page, _afterRenderable, _renderable);
	}

	@Override
	public void perform(final IDocumentEditor editor) {
		_page.removeRenderable(_renderable);
		// If we changed the front document -> goto changed page
		if (editor.getFrontDocument().hasPage(_page)) {
			editor.setCurrentPage(_page);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_afterRenderable);
		writer.writeObjectTable(_renderable);
	}

	@Override
	public String toString() {
		return String.format("RemoveRenderable: (Prev: %s) %s to %s", _afterRenderable, _renderable, _page);
	}
}
