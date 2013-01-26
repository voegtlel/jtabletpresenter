/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;

/**
 * @author lukas
 * 
 */
public class SetServerDocumentAction implements IAction {
	private final IEditableDocument _document;

	/**
	 * 
	 */
	public SetServerDocumentAction(final IEditableDocument document) {
		_document = document;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetServerDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		_document = reader.readObjectTable();
	}
	
	public SetClientDocumentAction getClientAction() {
		return new SetClientDocumentAction(_document);
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
		editor.setDocument(_document);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
	}

}
