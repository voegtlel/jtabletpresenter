package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import com.jmupdf.page.Page;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.intarsys.pdf.pd.PDPage;

public class PdfPageSerializable implements IEntity {
	private final long _id;
	private final DocumentPage _parent;
	private final PdfSerializable _basePdf;
	private final PDPage _page;
	private final Page _page2;

	public PdfPageSerializable(final DocumentPage parent, final PdfSerializable basePdf, final PDPage page, final Page page2) {
		_page2 = page2;
		_id = parent.getParent().nextId();
		_parent = parent;
		_basePdf = basePdf;
		_page = page;
	}
	
	public PdfPageSerializable(final DocumentPage parent, final PdfSerializable basePdf, final int pageIndex) throws IOException {
		this(parent, basePdf, basePdf.getDocument().getPageTree().getPageAt(pageIndex), basePdf.getDocument2().getPage(pageIndex + 1));
	}
	
	protected PdfPageSerializable(final DocumentPage parent, final PdfPageSerializable base) {
		this(parent, base._basePdf, base._page, base._page2);
	}
	
	public PdfSerializable getParentPdf() {
		return _basePdf;
	}
	
	public PDPage getPage() {
		return _page;
	}
	
	public Page getPage2() {
		return _page2;
	}
	
	public PdfPageSerializable(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.getParent().deserializeId(_id);
		_basePdf = reader.readObjectTable();
		final int pageIndex = reader.readInt();
		_page = _basePdf.getDocument().getPageTree().getPageAt(pageIndex);
		_page2 = _basePdf.getDocument2().getPage(pageIndex + 1);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);
		writer.writeObjectTable(_basePdf);
		System.out.println("Serialize pdf index " + _page.getNodeIndex());
		writer.writeInt(_page.getNodeIndex());
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public DocumentPage getParent() {
		return _parent;
	}
	
	/**
	 * Clones this object
	 * @param newDocument
	 * @return
	 */
	public PdfPageSerializable clone(final DocumentPage newDocument) {
		return new PdfPageSerializable(newDocument, this);
	}
}
