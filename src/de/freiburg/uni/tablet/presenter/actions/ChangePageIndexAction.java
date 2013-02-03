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
public class ChangePageIndexAction implements IAction {
	private final DocumentPage _page;
	private final DocumentPage _lastPage;

	/**
	 * 
	 */
	public ChangePageIndexAction(final DocumentPage page,
			final DocumentPage lastPage) {
		_page = page;
		_lastPage = lastPage;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePageIndexAction(final BinaryDeserializer reader)
			throws IOException {
		_page = reader.readObjectTable();
		_lastPage = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		// Disable undo
		return false;
	}

	@Override
	public IAction getUndoAction() {
		return new ChangePageIndexAction(_lastPage, _page);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		if ((editor.getCurrentPage() == _lastPage)
				|| (editor.getCurrentBackPage() == _lastPage)) {
			// Only change if we are at the last page
			editor.setCurrentPage(_page);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_lastPage);
	}

}
