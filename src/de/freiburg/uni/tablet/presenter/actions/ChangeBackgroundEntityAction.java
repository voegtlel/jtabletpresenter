/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;

/**
 * @author lukas
 * 
 */
public class ChangeBackgroundEntityAction implements IAction {
	private final DocumentPage _page;
	private final IEntity _backgroundEntity;
	private final IEntity _lastBackgroundEntity;

	/**
	 * 
	 */
	public ChangeBackgroundEntityAction(final DocumentPage page,
			final IEntity backgroundEntity, final IEntity lastBackgroundEntity) {
		_page = page;
		_backgroundEntity = backgroundEntity;
		_lastBackgroundEntity = lastBackgroundEntity;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangeBackgroundEntityAction(final BinaryDeserializer reader)
			throws IOException {
		_page = reader.readObjectTable();
		_backgroundEntity = reader.readObjectTable();
		_lastBackgroundEntity = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new ChangeBackgroundEntityAction(_page, _lastBackgroundEntity, _backgroundEntity);
	}

	@Override
	public void perform(final IDocumentEditor editor) {
		_page.setBackgroundEntity(_backgroundEntity);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_backgroundEntity);
		writer.writeObjectTable(_lastBackgroundEntity);
	}

	@Override
	public String toString() {
		return String.format("ChangeBackgroundEntity: (Last: %s) to %s in %s", _lastBackgroundEntity, _backgroundEntity, _page);
	}
}
