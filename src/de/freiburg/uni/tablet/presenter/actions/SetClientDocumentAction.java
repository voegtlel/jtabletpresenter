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
public class SetClientDocumentAction implements IAction {
	private final IEditableDocument _document;

	/**
	 * 
	 */
	public SetClientDocumentAction(final IEditableDocument document) {
		_document = document;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetClientDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		_document = reader.readObjectTable();
	}
	
	public SetServerDocumentAction getServerAction() {
		return new SetServerDocumentAction(_document);
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
		editor.setBaseDocument(_document);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
	}

}
