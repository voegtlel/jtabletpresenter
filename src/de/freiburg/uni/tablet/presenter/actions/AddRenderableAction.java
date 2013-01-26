/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

/**
 * @author lukas
 * 
 */
public class AddRenderableAction implements IAction {
	private final DocumentPage _page;
	private final IRenderable _afterRenderable;
	private final IRenderable _renderable;

	/**
	 * 
	 */
	public AddRenderableAction(final DocumentPage page,
			final IRenderable afterRenderable, final IRenderable renderable) {
		_page = page;
		_afterRenderable = afterRenderable;
		_renderable = renderable;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddRenderableAction(final BinaryDeserializer reader)
			throws IOException {
		_page = reader.readObjectTable();
		_afterRenderable = reader.readObjectTable();
		_renderable = reader.readObjectTable();
	}
	
	@Override
	public boolean mustRedraw(DocumentEditor editor) {
		return editor.getCurrentPage() == _page;
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new RemoveRenderableAction(_page, _afterRenderable, _renderable);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		_page.insertRenderable(_afterRenderable, _renderable);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_afterRenderable);
		writer.writeObjectTable(_renderable);
	}

}
