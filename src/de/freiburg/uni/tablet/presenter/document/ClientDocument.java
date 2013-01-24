package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;

public class ClientDocument extends Document {
	/**
	 * Create a new server document.
	 */
	public ClientDocument(final int docId) {
		super(docId);
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected ClientDocument(final int docId, final ClientDocument base) {
		super(docId, base);
	}
	
	public ClientDocument(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	public ClientDocument clone(final int docId) {
		return new ClientDocument(docId, this);
	}
}
