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
import de.freiburg.uni.tablet.presenter.document.IEntity;

/**
 * @author lukas
 * 
 */
public class AddObjectAction implements IAction, IBinarySerializable {
	private final IEntity _entity;

	/**
	 * 
	 */
	public AddObjectAction(final IEntity entity) {
		_entity = entity;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddObjectAction(final BinaryDeserializer reader) throws IOException {
		_entity = reader.readSerializableClass();
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
		editor.getDocument().onObjectAdded(_entity);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeSerializableClass(_entity);
	}
}
