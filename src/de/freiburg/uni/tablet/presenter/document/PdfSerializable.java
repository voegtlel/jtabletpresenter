package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.ByteArrayLocator;

public class PdfSerializable implements IEntity {
	private final long _id;
	private final IDocument _parent;
	private boolean _documentLoaded = false;
	private PDDocument _document = null;
	private final byte[] _data;
	
	public PdfSerializable(final IDocument parent, final File srcFile) throws IOException {
		_id = parent.nextId();
		_parent = parent;
		_data = FileHelper.readFile(srcFile);
	}
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param document
	 */
	private PdfSerializable(final IDocument parent, final PDDocument document, final byte[] data) {
		_id = parent.nextId();
		_parent = parent;
		_document = document;
		_data = data;
	}
	
	/**
	 * Gets the lazy-load pdf document
	 * @return
	 */
	public PDDocument getDocument() {
		if (!_documentLoaded) {
			_documentLoaded = true;
			final ByteArrayLocator loc = new ByteArrayLocator(_data, "virtual", "pdf");
			try {
				_document = PDDocument.createFromLocator(loc);
			} catch (COSLoadException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					loc.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return _document;
	}
	
	public PdfSerializable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.deserializeId(_id);
		final boolean pdfAvailable = reader.readBoolean();
		if (pdfAvailable) {
			_data = reader.readByteArray();
		} else {
			_data = null;
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
		
		if (_data != null) {
			writer.writeBoolean(true);
			writer.writeByteArray(_data, 0, _data.length);
		} else {
			writer.writeBoolean(false);
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
