/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

/**
 * @author lukas
 * 
 */
public class DocumentPage implements IEntity {
	private final long _id;
	private final Document _document;

	/**
	 * 
	 */
	public DocumentPage(final Document document) {
		_document = document;
		_id = _document.getNextId();
	}

	@Override
	public long getId() {
		return _id;
	}
}
