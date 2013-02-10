/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.DocumentClient;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;

/**
 * @author lukas
 * 
 */
public class SetServerDocumentAction implements IAction {
	private final IClientDocument _document;
	private final DocumentPage _currentPage;

	/**
	 * Action for forwarding the active document
	 */
	public SetServerDocumentAction(final IClientDocument document, final DocumentPage currentPage) {
		_document = document;
		_currentPage = currentPage;
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
		editor.setBackDocument(_document);
		editor.setCurrentPage(_currentPage);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_document, DocumentClient.class);
		writer.writeObjectTable(_currentPage);
	}
	
	@Override
	public String toString() {
		return String.format("SetDocument: to %s, currentPage: %X", _document.getUuid(), (_currentPage==null?0:_currentPage.getId()));
	}
}
