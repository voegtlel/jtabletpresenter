/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

/**
 * @author lukas
 * 
 */
public class AddPageAction implements IAction {
	private final DocumentPage _prevPage;
	private final DocumentPage _page;

	/**
	 * 
	 */
	public AddPageAction(final DocumentPage prevPage, final DocumentPage page) {
		_prevPage = prevPage;
		_page = page;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddPageAction(final BinaryDeserializer reader) throws IOException {
		_prevPage = reader.readObjectTable();
		_page = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new RemovePageAction(_prevPage, _page);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		editor.getDocument().insertPage(_prevPage, _page);
	}
	
	@Override
	public boolean mustRedraw(DocumentEditor editor) {
		return false;
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_prevPage);
		writer.writeObjectTable(_page);
	}
}
