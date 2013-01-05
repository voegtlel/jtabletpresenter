package de.freiburg.uni.tablet.presenter.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.FileLocator;
import de.intarsys.tools.locator.ILocator;
import de.intarsys.tools.locator.StreamLocator;

public class PdfSerializable implements IEntity {
	private final long _id;
	private final IEntity _parent;
	private final PDDocument _document;

	public PdfSerializable(final long id, final IEntity parent) {
		_id = id;
		_parent = parent;
		_document = null;
	}
	
	public PdfSerializable(final long id, final IEntity parent, final PDDocument document) {
		_id = id;
		_parent = parent;
		_document = document;
	}
	
	public PdfSerializable(final long id, final IEntity parent, final File srcFile) throws IOException {
		_id = id;
		_parent = parent;
		try {
			_document = PDDocument.createFromLocator(new FileLocator(srcFile));
		} catch (COSLoadException e) {
			throw new IOException(e);
		}
	}
	
	public PDDocument getDocument() {
		return _document;
	}
	
	public PdfSerializable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		int pdfNullCheck = reader.readInt();
		if (pdfNullCheck == 0) {
			_document = null;
		} else {
			ByteArrayInputStream bis = new ByteArrayInputStream(reader.readByteArray());
			ILocator loc = new StreamLocator(bis, "virtual", "pdf");
			try {
				_document = PDDocument.createFromLocator(loc);
			} catch (COSLoadException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent.getId(), _parent);
		
		if (_document == null) {
			writer.writeInt(0);
		} else {
			writer.writeInt(1);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ILocator loc = new StreamLocator(bos, "virtual", "pdf");
		_document.save(loc);
		writer.writeByteArray(bos.toByteArray());
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public IEntity getParent() {
		return _parent;
	}
}
