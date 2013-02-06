/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.ClientDocument;
import de.freiburg.uni.tablet.presenter.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;

/**
 * @author lukas
 * 
 */
public class SetServerDocumentAction implements IAction {
	private final IDocument _document;
	private final int _currentPageIndex;

	/**
	 * Action for forwarding the active document
	 */
	public SetServerDocumentAction(final IClientDocument document, final int currentPageIndex) {
		_document = document;
		_currentPageIndex = currentPageIndex;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetServerDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		reader.resetState();
		_document = reader.readObjectTable();
		_currentPageIndex = reader.readInt();
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
		editor.setDocument(_document);
		editor.setCurrentPageByIndex(_currentPageIndex, false);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_document, ClientDocument.class);
		writer.writeInt(_currentPageIndex);
	}
}
