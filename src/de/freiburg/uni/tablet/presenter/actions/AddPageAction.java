/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.IDocumentEditor;

/**
 * @author lukas
 * 
 */
public class AddPageAction implements IAction {
	private final IClientDocument _document;
	private final DocumentPage _prevPage;
	private final DocumentPage _page;

	/**
	 * 
	 */
	public AddPageAction(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
		_document = document;
		_prevPage = prevPage;
		_page = page;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddPageAction(final BinaryDeserializer reader) throws IOException {
		_document = reader.readObjectTable();
		_prevPage = reader.readObjectTable();
		_page = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new RemovePageAction(_document, _prevPage, _page);
	}

	@Override
	public void perform(final IDocumentEditor editor) {
		_document.insertPage(_prevPage, _page);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document);
		writer.writeObjectTable(_prevPage);
		writer.writeObjectTable(_page);
	}
}
