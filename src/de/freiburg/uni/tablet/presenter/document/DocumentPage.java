/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

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

	@Override
	public Document getParent() {
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

	public DocumentPage(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		reader.putObjectTable(this.getId(), this);
		_document = reader.readObjectTable();
		_clientOnlyLayer = reader.readObjectTable();
		_serverSyncLayer = reader.readObjectTable();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_document.getId(), _document);
		writer.writeObjectTable(_clientOnlyLayer.getId(), _clientOnlyLayer);
		writer.writeObjectTable(_serverSyncLayer.getId(), _serverSyncLayer);
	}

	public DocumentPage(final BinaryDeserializer reader, final Document document)
			throws IOException {
		_id = reader.readLong();
		_document = document;
		reader.putObjectTable(this.getId(), this);
		_clientOnlyLayer = reader.readObjectTable();
		_serverSyncLayer = reader.readObjectTable();
	}

	/**
	 * Serialize this object directly, without its parent
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void serializeDirect(final BinarySerializer writer)
			throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_clientOnlyLayer.getId(), _clientOnlyLayer);
		writer.writeObjectTable(_serverSyncLayer.getId(), _serverSyncLayer);
	}
}
