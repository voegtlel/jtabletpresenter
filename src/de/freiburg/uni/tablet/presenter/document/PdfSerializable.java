package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.interfaces.Page;
import com.jmupdf.pdf.PdfDocument;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocument;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocumentPageEntity;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.tools.locator.ByteArrayLocator;

public class PdfSerializable extends AbstractEntity implements IBackDocument {
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
		loadDocument();
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
		loadDocument();
		if (_document2 != null) {
			try {
				return _document2.getPage(index);
			} catch (PageException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Gets the lazy-load pdf document
	 * @return
	 */
	public PdfDocument getDocument2() {
		loadDocument();
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
	
	@Override
	public Iterable<IBackDocumentPage> getPagesAt(final int pageIndex) {
		loadDocument();
		if (_document == null) {
			return new ArrayList<>();
		}
		return new Iterable<IBackDocumentPage>() {
			@Override
			public Iterator<IBackDocumentPage> iterator() {
				return new Iterator<IBackDocumentPage>() {
					PDPage page = null;
					
					@Override
					public IBackDocumentPage next() {
						if (page == null) {
							page = _document.getPageTree().getPageAt(pageIndex);
						} else {
							page = page.getNextPage();
						}
						return new LocalDocumentPage(page);
					}
					
					@Override
					public boolean hasNext() {
						return (page == null && _document.getPageTree().getCount() > pageIndex) || (page != null && page.getNextPage() != null);
					}
				};
			}
		};
	}

	@Override
	public Iterable<IBackDocumentPage> getPages() {
		return getPagesAt(0);
	}
	
	/**
	 * Internal class for a page
	 *
	 */
	private class LocalDocumentPage implements IBackDocumentPage {
		PDPage _page;
		
		public LocalDocumentPage(final PDPage page) {
			_page = page;
		}
		
		@Override
		public IBackDocumentPageEntity createEntity(final DocumentPage parent) {
			return new PdfPageSerializable(parent, PdfSerializable.this, _page, tryGetPage(_page.getNodeIndex() + 1));
		}

		@Override
		public int getPageIndex() {
			return _page.getNodeIndex();
		}
	}

	@Override
	public int getPageCount() {
		loadDocument();
		if (_document == null) {
			return 0;
		}
		return _document.getPageTree().getCount();
	}
}
