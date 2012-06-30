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

	private final DocumentPageLayer _clientOnlyLayer;
	private final DocumentPageLayer _serverSyncLayer;

	/**
	 * 
	 */
	public DocumentPage(final Document document) {
		_document = document;
		_id = _document.getNextId();
		_clientOnlyLayer = new DocumentPageLayer(this);
		_serverSyncLayer = new DocumentPageLayer(this);
		_document.onObjectAdded(getClientOnlyLayer());
		_document.onObjectAdded(getServerSyncLayer());
	}

	@Override
	public long getId() {
		return _id;
	}

	/**
	 * Gets the parent document
	 * 
	 * @return
	 */
	public Document getDocument() {
		return _document;
	}

	/**
	 * Gets the client only layer
	 * 
	 * @return
	 */
	public DocumentPageLayer getClientOnlyLayer() {
		return _clientOnlyLayer;
	}

	/**
	 * Gets the server synchronized layer
	 * 
	 * @return
	 */
	public DocumentPageLayer getServerSyncLayer() {
		return _serverSyncLayer;
	}
}
