/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

/**
 * @author lukas
 * 
 */
public class AddPageAction extends AbstractAction implements IAction, IBinarySerializable {
	private final DocumentPage _prevPage;
	private final DocumentPage _page;

	/**
	 * 
	 */
	public AddPageAction(int clientId, final DocumentPage prevPage, final DocumentPage page) {
		super(clientId);
		_prevPage = prevPage;
		_page = page;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public AddPageAction(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_prevPage = reader.readObjectTable();
		_page = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction(int clientId) {
		return new RemovePageAction(clientId, _prevPage, _page);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		editor.getDocument().insertPage(_prevPage, _page);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_prevPage.getId(), _prevPage);
		writer.writeObjectTable(_page.getId(), _page);
	}
}
