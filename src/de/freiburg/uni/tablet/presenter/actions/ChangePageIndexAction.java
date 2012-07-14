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
public class ChangePageIndexAction implements IAction, IBinarySerializable {
	private final long _pageId;
	private final long _lastPageId;

	/**
	 * 
	 */
	public ChangePageIndexAction(final long pageId, final long lastPageId) {
		_pageId = pageId;
		_lastPageId = lastPageId;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePageIndexAction(final BinaryDeserializer reader)
			throws IOException {
		_pageId = reader.readLong();
		_lastPageId = reader.readLong();
	}

	@Override
	public boolean hasUndoAction() {
		return _lastPageId != -1;
	}

	@Override
	public IAction getUndoAction() {
		return new ChangePageIndexAction(_lastPageId, _pageId);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		final DocumentPage page = (DocumentPage) editor.getDocument()
				.getObject(_pageId);
		if (page == null) {
			throw new IllegalStateException("Page with id " + _pageId
					+ " doesn't exist");
		}
		editor.setCurrentPage(page);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_pageId);
		writer.writeLong(_lastPageId);
	}

}
