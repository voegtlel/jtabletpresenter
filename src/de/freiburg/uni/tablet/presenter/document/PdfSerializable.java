package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.PDDocument;
import de.freiburg.uni.tablet.presenter.PDPage;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public class PdfSerializable implements IEntity {
	private final long _id;
	private final IDocument _parent;
	private final PDDocument _document;
	
	private byte[] _data;
	
	public PdfSerializable(final IDocument parent, final PDDocument document) {
		_id = parent.nextId();
		_parent = parent;
		_document = document;
	}
	
	public PdfSerializable(final IDocument parent, final File srcFile) throws IOException {
		_id = parent.nextId();
		_parent = parent;
		_document = null;
		/*try {
			_document = PDDocument.createFromLocator(new FileLocator(srcFile));
		} catch (COSLoadException e) {
			throw new IOException(e);
		}*/
	}
	
	public PDDocument getDocument() {
		return _document;
	}
	
	public PDPage getPageAt(final int index) {
		return new PDPage(index);
	}
	
	public PdfSerializable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.deserializeId(_id);
		final int pdfNullCheck = reader.readInt();
		if (pdfNullCheck == 0) {
			_document = null;
		} else {
			_document = new PDDocument();
			_data = reader.readByteArray();
			// TODO: We should never get here
			/*final byte[] data = reader.readByteArray();
			final ByteArrayLocator loc = new ByteArrayLocator(data, "virtual", "pdf");
			try {
				_document = PDDocument.createFromLocator(loc);
			} catch (COSLoadException e) {
				throw new IOException(e);
			}
			loc.delete();*/
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
		
		if (_data == null) {
			writer.writeInt(0);
		} else {
			writer.writeInt(1);
			writer.writeByteArray(_data, 0, _data.length);
			// TODO: We should never get here
			/*final ByteArrayLocator loc = new ByteArrayLocator(null, "virtual", "pdf");
			_document.save(loc);
			writer.writeByteArray(loc.getContent(), 0, (int)loc.getLength());
			loc.delete();*/
		}
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public IDocument getParent() {
		return _parent;
	}
	
	/**
	 * Clones this object
	 * @param newDocument
	 * @return
	 */
	public PdfSerializable clone(final IDocument newDocument) {
		return new PdfSerializable(newDocument, _document);
	}
}
