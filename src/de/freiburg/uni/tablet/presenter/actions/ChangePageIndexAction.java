/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.Document;

/**
 * @author lukas
 * 
 */
public class ChangePageIndexAction implements IAction, IBinarySerializable {
	private final int _pageId;

	/**
	 * 
	 */
	public ChangePageIndexAction(final int pageId) {
		_pageId = pageId;
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return null;
	}

	@Override
	public void perform(final Document document) {
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
	}

}
