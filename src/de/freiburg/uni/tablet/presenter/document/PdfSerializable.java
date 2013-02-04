package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.ByteArrayLocator;
import de.intarsys.tools.locator.FileLocator;

public class PdfSerializable implements IEntity {
	private final long _id;
	private final IDocument _parent;
	private final PDDocument _document;
	private final byte[] _data;
	
	public PdfSerializable(final IDocument parent, final File srcFile) throws IOException {
		_id = parent.nextId();
		_parent = parent;
		try {
			_data = FileHelper.readFile(srcFile);
			_document = PDDocument.createFromLocator(new FileLocator(srcFile));
		} catch (COSLoadException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param document
	 */
	private PdfSerializable(final IDocument parent, final PDDocument document, byte[] data) {
		_id = parent.nextId();
		_parent = parent;
		_document = document;
		_data = data;
	}
	
	
	public PDDocument getDocument() {
		return _document;
	}
	
	public PdfSerializable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.deserializeId(_id);
		final boolean pdfAvailable = reader.readBoolean();
		if (pdfAvailable) {
			_document = null;
			_data = null;
		} else {
			_data = reader.readByteArray();
			final ByteArrayLocator loc = new ByteArrayLocator(_data, "virtual", "pdf");
			try {
				_document = PDDocument.createFromLocator(loc);
			} catch (COSLoadException e) {
				throw new IOException(e);
			}
			loc.delete();
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
		
		if (_data == null) {
			writer.writeBoolean(false);
		} else {
			writer.writeBoolean(true);
			writer.writeByteArray(_data, 0, _data.length);
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
		return new PdfSerializable(newDocument, _document, _data);
	}
}
