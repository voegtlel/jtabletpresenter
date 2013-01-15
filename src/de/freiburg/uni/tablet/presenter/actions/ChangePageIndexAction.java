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
public class ChangePageIndexAction extends AbstractAction implements IAction, IBinarySerializable {
	private final DocumentPage _page;
	private final DocumentPage _lastPage;

	/**
	 * 
	 */
	public ChangePageIndexAction(int clientId, final DocumentPage page,
			final DocumentPage lastPage) {
		super(clientId);
		_page = page;
		_lastPage = lastPage;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePageIndexAction(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
		_page = reader.readObjectTable();
		_lastPage = reader.readObjectTable();
	}
	
	@Override
	public boolean mustRedraw(DocumentEditor editor) {
		return true;
	}

	@Override
	public boolean hasUndoAction() {
		return _lastPage != null;
	}

	@Override
	public IAction getUndoAction(int clientId) {
		return new ChangePageIndexAction(clientId, _lastPage, _page);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		editor.setCurrentPage(_page);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_page.getId(), _page);
		writer.writeObjectTable(_lastPage.getId(), _lastPage);
	}

}
