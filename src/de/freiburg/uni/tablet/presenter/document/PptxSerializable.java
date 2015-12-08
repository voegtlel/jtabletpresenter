package de.freiburg.uni.tablet.presenter.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocument;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocumentPageEntity;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;

public class PptxSerializable extends AbstractEntity implements IBackDocument {
	private boolean _documentLoaded = false;
	private XMLSlideShow _document = null;
	private final byte[] _data;
	
	public PptxSerializable(final IDocument parent, final File srcFile) throws IOException {
		super(parent);
		_data = FileHelper.readFile(srcFile);
	}
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param document
	 */
	private PptxSerializable(final IDocument parent, final XMLSlideShow document, final byte[] data) {
		super(parent);
		_document = document;
		_documentLoaded = (document != null);
		_data = data;
	}
	
	/**
	 * Gets the lazy-load pdf document
	 * @return
	 */
	public XMLSlideShow getDocument() {
		loadDocument();
		return _document;
	}
	
	/**
	 * Loads the internal documents
	 */
	private void loadDocument() {
		if (!_documentLoaded) {
			_documentLoaded = true;
			try {
				_document = new XMLSlideShow(new ByteArrayInputStream(_data));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Tries to load the page
	 * @param index
	 * @return
	 */
	public XSLFSlide tryGetSlide(final int index) {
		loadDocument();
		if (_document == null || index < 0 || index >= _document.getSlides().size()) {
			return null;
		}
		return _document.getSlides().get(index);
	}
	
	public PptxSerializable(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_data = reader.readByteArray();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeByteArray(_data, 0, _data.length);
	}

	
	/**
	 * Clones this object
	 * @param newDocument
	 * @return
	 */
	public PptxSerializable clone(final IDocument newDocument) {
		return new PptxSerializable(newDocument, _document, _data);
	}
	
	@Override
	public Iterable<IBackDocumentPage> getPagesAt(final int pageIndex) {
		loadDocument();
		if (_document == null) {
			return new ArrayList<IBackDocumentPage>();
		}
		return new Iterable<IBackDocumentPage>() {
			@Override
			public Iterator<IBackDocumentPage> iterator() {
				return new Iterator<IBackDocumentPage>() {
					private Iterator<XSLFSlide> _baseIterator = _document.getSlides().listIterator(pageIndex);
					
					@Override
					public IBackDocumentPage next() {
						XSLFSlide slide = _baseIterator.next();
						return new InternalBackPage(slide);
					}
					
					@Override
					public boolean hasNext() {
						return _baseIterator.hasNext();
					}
				};
			}
		};
	}

	@Override
	public Iterable<IBackDocumentPage> getPages() {
		return getPagesAt(0);
	}
	
	private class InternalBackPage implements IBackDocumentPage {
		private XSLFSlide _slide;

		public InternalBackPage(final XSLFSlide slide) {
			_slide = slide;
		}

		@Override
		public IBackDocumentPageEntity createEntity(final DocumentPage parent) {
			return new PptxSlideSerializable(parent, PptxSerializable.this, _slide);
		}

		@Override
		public int getPageIndex() {
			return _slide.getSlideNumber() - 1;
		}
	}
	
	@Override
	public int getPageCount() {
		loadDocument();
		if (_document == null) {
			return 0;
		}
		return _document.getSlides().size();
	}
}
