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
	
	private int _pdfPageIndex = -1;

	/**
	 * 
	 */
	public DocumentPage(final Document document) {
		_document = document;
		_id = _document.getNextId();
		_clientOnlyLayer = new DocumentPageLayer(this);
		_serverSyncLayer = new DocumentPageLayer(this);
	}

	/**
	 * 
	 */
	public DocumentPage(final long newId, final Document document) {
		_document = document;
		_id = newId;
		_clientOnlyLayer = new DocumentPageLayer(this);
		_serverSyncLayer = new DocumentPageLayer(this);
	}
	
	/**
	 * Ctor for cloning
	 */
	private DocumentPage(final Document document, final DocumentPageLayer clientOnlyLayer, final DocumentPageLayer serverSyncLayer) {
		_document = document;
		_id = document.getNextId();
		_clientOnlyLayer = clientOnlyLayer.clone(this);
		_serverSyncLayer = serverSyncLayer.clone(this);
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
	
	/**
	 * Gets if the page is empty
	 * @return
	 */
	public boolean isEmpty() {
		return _clientOnlyLayer.isEmpty() && _serverSyncLayer.isEmpty();
	}
	
	/**
	 * Sets the index for a referenced pdf page
	 * @param pdfPageIndex
	 */
	public void setPdfPageIndex(int pdfPageIndex) {
		int lastPdfPageIndex = _pdfPageIndex;
		_pdfPageIndex = pdfPageIndex;
		_document.firePdfPageIndexChanged(this, pdfPageIndex, lastPdfPageIndex);
	}
	
	/**
	 * Gets the index for a referenced pdf page
	 * @return
	 */
	public int getPdfPageIndex() {
		return _pdfPageIndex;
	}

	public DocumentPage(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		reader.putObjectTable(this.getId(), this);
		_pdfPageIndex = reader.readInt();
		_document = reader.readObjectTable();
		_clientOnlyLayer = reader.readObjectTable();
		_serverSyncLayer = reader.readObjectTable();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeInt(_pdfPageIndex);
		writer.writeObjectTable(_document.getId(), _document);
		writer.writeObjectTable(_clientOnlyLayer.getId(), _clientOnlyLayer);
		writer.writeObjectTable(_serverSyncLayer.getId(), _serverSyncLayer);
	}

	/**
	 * Deserialize only this page
	 * @param reader
	 * @param dummy null
	 * @throws IOException
	 */
	public DocumentPage(final BinaryDeserializer reader, final Document dummy)
			throws IOException {
		_id = reader.readLong();
		_document = null;
		reader.putObjectTable(_id, this);
		_pdfPageIndex = reader.readInt();
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
		writer.putObjectTable(_id, this);
		writer.writeLong(_id);
		writer.writeInt(_pdfPageIndex);
		writer.writeObjectTable(_clientOnlyLayer.getId(), _clientOnlyLayer);
		writer.writeObjectTable(_serverSyncLayer.getId(), _serverSyncLayer);
	}
	
	/**
	 * Clone this object to the dstDocument
	 * @param dstDocument
	 * @return
	 */
	public DocumentPage clone(Document dstDocument) {
		return new DocumentPage(dstDocument, _clientOnlyLayer, _serverSyncLayer);
	}
}
