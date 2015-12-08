package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import org.apache.poi.xslf.usermodel.XSLFSlide;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IBackDocumentPageEntity;

public class PptxSlideSerializable extends AbstractPageEntity implements IBackDocumentPageEntity {
	private final PptxSerializable _basePptx;
	private final XSLFSlide _slide;

	public PptxSlideSerializable(final DocumentPage parent, final PptxSerializable basePptx, final XSLFSlide slide) {
		super(parent);
		_basePptx = basePptx;
		_slide = slide;
	}
	
	public PptxSlideSerializable(final DocumentPage parent, final PptxSerializable basePptx, final int slideIndex) throws IOException {
		this(parent, basePptx, basePptx.tryGetSlide(slideIndex));
	}
	
	protected PptxSlideSerializable(final DocumentPage parent, final PptxSlideSerializable base) {
		this(parent, base._basePptx, base._slide);
	}
	
	public PptxSerializable getParentPptx() {
		return _basePptx;
	}
	
	public XSLFSlide getSlide() {
		return _slide;
	}
	
	public PptxSlideSerializable(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_basePptx = reader.readObjectTable();
		final int pageIndex = reader.readInt();
		_slide = _basePptx.tryGetSlide(pageIndex);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_basePptx);
		System.out.println("Serialize slide index " + getPageIndex());
		writer.writeInt(getPageIndex());
	}
	
	/**
	 * Clones this object
	 * @param newDocument
	 * @return
	 */
	public PptxSlideSerializable clone(final DocumentPage newDocument) {
		return new PptxSlideSerializable(newDocument, this);
	}
	
	@Override
	public int getPageIndex() {
		return _slide.getSlideNumber() - 1;
	}
}
