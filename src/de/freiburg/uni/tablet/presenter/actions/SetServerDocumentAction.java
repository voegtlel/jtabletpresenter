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
	private final int _currentPageIndex;

	/**
	 * 
	 */
	public SetServerDocumentAction(final IEditableDocument document, final int currentPageIndex) {
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
	
	public SetClientDocumentAction getClientAction() {
		return new SetClientDocumentAction(_document, _currentPageIndex);
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
		editor.setCurrentPageByIndex(_currentPageIndex, false);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.resetState();
		writer.writeObjectTable(_document);
		writer.writeInt(_currentPageIndex);
	}
}
