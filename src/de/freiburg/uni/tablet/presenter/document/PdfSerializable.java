package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.pdf.PdfDocument;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.ByteArrayLocator;

public class PdfSerializable extends AbstractEntity {
	private boolean _documentLoaded = false;
	private PDDocument _document = null;
	private PdfDocument _document2 = null;
	private final byte[] _data;
	
	public PdfSerializable(final IDocument parent, final File srcFile) throws IOException {
		super(parent);
		_data = FileHelper.readFile(srcFile);
	}
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param document
	 */
	private PdfSerializable(final IDocument parent, final PDDocument document, final PdfDocument document2, final byte[] data) {
		super(parent);
		_document = document;
		_document2 = document2;
		_documentLoaded = (document != null);
		_data = data;
	}
	
	/**
	 * Gets the lazy-load pdf document
	 * @return
	 */
	public PDDocument getDocument() {
		if (!_documentLoaded) {
			loadDocument();
		}
		return _document;
	}
	
	/**
	 * Loads the internal documents
	 */
	private void loadDocument() {
		if (!_documentLoaded) {
			_documentLoaded = true;
			final ByteArrayLocator loc = new ByteArrayLocator(_data, "virtual", "pdf");
			try {
				_document = PDDocument.createFromLocator(loc);
			} catch (COSLoadException | IOException e) {
				e.printStackTrace();
			} finally {
				try {
					loc.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				_document2 = new PdfDocument(_data);
			} catch (DocException | DocSecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Tries to load the page
	 * @param index
	 * @return
	 */
	public Page tryGetPage(final int index) {
		try {
			return getDocument2().getPage(index);
		} catch (PageException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets the lazy-load pdf document
	 * @return
	 */
	public PdfDocument getDocument2() {
		if (!_documentLoaded) {
			loadDocument();
		}
		return _document2;
	}
	
	public PdfSerializable(final BinaryDeserializer reader) throws IOException {
		super(reader);
		final boolean pdfAvailable = reader.readBoolean();
		if (pdfAvailable) {
			_data = reader.readByteArray();
		} else {
			_data = null;
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		
		if (_data != null) {
			writer.writeBoolean(true);
			writer.writeByteArray(_data, 0, _data.length);
		} else {
			writer.writeBoolean(false);
		}
	}

	
	/**
	 * Clones this object
	 * @param newDocument
	 * @return
	 */
	public PdfSerializable clone(final IDocument newDocument) {
		return new PdfSerializable(newDocument, _document, _document2, _data);
	}
}
