package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;

public class ClientDocument extends Document {
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected ClientDocument(final int docId, final ClientDocument base) {
		super(docId, base);
	}
	
	/**
	 * Deserialize an initial server document
	 * @param reader
	 * @throws IOException
	 */
	public ClientDocument(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	public ClientDocument clone(final int docId) {
		return new ClientDocument(docId, this);
	}
}
