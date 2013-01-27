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
	private final int _currentPageIndex;

	/**
	 * 
	 */
	public SetClientDocumentAction(final IEditableDocument document, final int currentPageIndex) {
		_document = document;
		_currentPageIndex = currentPageIndex;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public SetClientDocumentAction(final BinaryDeserializer reader)
			throws IOException {
		reader.resetState();
		_document = reader.readObjectTable();
		_currentPageIndex = reader.readInt();
	}
	
	public SetServerDocumentAction getServerAction() {
		return new SetServerDocumentAction(_document, _currentPageIndex);
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
