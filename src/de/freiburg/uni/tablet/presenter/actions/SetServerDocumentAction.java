/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.ClientDocument;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;

/**
 * @author lukas
 * 
 */
public class SetServerDocumentAction implements IAction {
	private final static int DST_BACK_DOCUMENT = 0;
	private final static int DST_FRONT_DOCUMENT = 1;
	
	private final IClientDocument _document;
	private final DocumentPage _currentPage;
	private final int _destination;

	/**
	 * Action for forwarding the active document
	 */
	public SetServerDocumentAction(final IClientDocument document, final DocumentPage currentPage, final int destination) {
		_document = document;
		_currentPage = currentPage;
		_destination = destination;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetServerDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		reader.resetState();
		_document = reader.readObjectTable();
		_currentPage = reader.readObjectTable();
		_destination = reader.readInt();
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
	public void perform(final IDocumentEditor editor) {
		if (_destination == DST_FRONT_DOCUMENT) {
			editor.setDocument(_document);
		} else if (_destination == DST_BACK_DOCUMENT) {
			editor.setBaseDocument(_document);
		}
		editor.setCurrentPage(_currentPage);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_document, ClientDocument.class);
		writer.writeObjectTable(_currentPage);
		writer.writeInt(_destination);
	}
}
