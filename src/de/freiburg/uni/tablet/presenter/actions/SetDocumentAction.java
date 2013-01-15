/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

/**
 * @author lukas
 * 
 */
public class SetDocumentAction extends AbstractAction implements IAction, IBinarySerializable {
	private Document _document;

	/**
	 * 
	 */
	public SetDocumentAction(int clientId, final Document document) {
		super(clientId);
		_document = document;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
		_document = reader.readObjectTable();
	}
	
	@Override
	public boolean mustRedraw(DocumentEditor editor) {
		return true;
	}

	@Override
	public boolean hasUndoAction() {
		return false;
	}

	@Override
	public IAction getUndoAction(int clientId) {
		return null;
	}

	@Override
	public void perform(final DocumentEditor editor) {
		editor.setDocument(_document);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_document.getId(), _document);
	}

}
