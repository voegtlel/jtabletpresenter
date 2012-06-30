/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

/**
 * @author lukas
 * 
 */
public class DeleteObjectAction implements IAction, IBinarySerializable {

	/**
	 * 
	 */
	public DeleteObjectAction(final int pageId, final int lastPageId) {
		_pageId = pageId;
		_lastPageId = lastPageId;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public DeleteObjectAction(final BinaryDeserializer reader)
			throws IOException {
		_pageId = reader.readInt();
		_lastPageId = reader.readInt();
	}

	@Override
	public boolean hasUndoAction() {
		return _lastPageId != -1;
	}

	@Override
	public IAction getUndoAction() {
		return new DeleteObjectAction(_lastPageId, _pageId);
	}

	@Override
	public void perform(final Document document) {
		final DocumentPage page = (DocumentPage) document.getObject(_pageId);
		if (page == null) {
			throw new IllegalStateException("Page with id " + _pageId
					+ " doesn't exist");
		}
		document.setCurrentPage(page);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_pageId);
		writer.writeInt(_lastPageId);
	}

}
