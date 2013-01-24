/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

/**
 * @author lukas
 * 
 */
public class DocumentPage implements IEntity, IPageRepaintListener {
	private final long _id;
	private final IDocumentNotify _document;

	private final LinkedElementList<IRenderable> _renderablesList = new LinkedElementList<IRenderable>();
	private final HashMap<Long, IRenderable> _renderablesMap = new HashMap<Long, IRenderable>();
	
	private PdfPageSerializable _pdfPage = null;

	/**
	 * Creates a new document page for a document. The page will not be added to the document.
	 */
	public DocumentPage(final IDocumentNotify document) {
		_document = document;
		_id = _document.nextId();
	}
	
	/**
	 * Ctor for cloning
	 */
	private DocumentPage(final IDocumentNotify document, final DocumentPage base) {
		this(document);
		for (LinkedElement<IRenderable> r = base._renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			final IRenderable newRenderable = r.getData().cloneRenderable(_document.nextId());
			_renderablesList.addLast(newRenderable);
			_renderablesMap.put(newRenderable.getId(), newRenderable);
		}
	}

	@Override
	public IDocument getParent() {
		return _document;
	}

	public void render(final IPageBackRenderer renderer) {
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			r.getData().render(renderer);
		}
	}

	public void eraseAt(final EraseInfo eraseInfo) {
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			r.getData().eraseAt(eraseInfo);
		}
	}

	public IRenderable getRenderable(final long id) {
		return _renderablesMap.get(id);
	}

	public void addRenderable(final IRenderable renderable) {
		_renderablesMap.put(renderable.getId(), renderable);
		_renderablesList.addLast(renderable);
		_document.fireRenderableAdded(renderable, this);
	}

	public void removeRenderable(final IRenderable renderable) {
		_renderablesMap.remove(renderable.getId());
		_renderablesList.remove(_renderablesList.getElement(renderable));
		_document.fireRenderableRemoved(renderable, this);
	}
	
	/**
	 * Sets the index for a referenced pdf page
	 * @param pdfPageIndex
	 */
	public void setPdfPage(final PdfPageSerializable page) {
		final PdfPageSerializable lastPdfPage = _pdfPage;
		_pdfPage = page;
		_document.firePdfPageChanged(this, lastPdfPage);
	}
	
	/**
	 * Gets the index for a referenced pdf page
	 * @return
	 */
	public PdfPageSerializable getPdfPage() {
		return _pdfPage;
	}
	
	/**
	 * Gets the renderables
	 * @return
	 */
	public LinkedElementList<IRenderable> getRenderables() {
		return _renderablesList;
	}
	
	/**
	 * Clears the page
	 */
	public void clear() {
		while (!_renderablesList.isEmpty()) {
			final IRenderable removeElement = _renderablesList.getFirst().getData();
			_renderablesList.removeFirst();
			_document.fireRenderableRemoved(removeElement, this);
		}
		_renderablesMap.clear();
	}
	
	/**
	 * Checks if the page is empty
	 * @return
	 */
	public boolean isEmpty() {
		return _renderablesList.isEmpty();
	}

	@Override
	public long getId() {
		return _id;
	}

	public DocumentPage(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
		reader.putObjectTable(this.getId(), this);
		_document = reader.readObjectTable();
		_pdfPage = reader.readObjectTable();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IRenderable renderable = reader.readObjectTable();
			_renderablesList.addLast(renderable);
			_renderablesMap.put(renderable.getId(), renderable);
		}
	}
	
	/**
	 * Deserialize only this page without its document.
	 * @param reader
	 * @param dummy null
	 * @throws IOException
	 */
	public DocumentPage(final BinaryDeserializer reader, final Document dummy)
			throws IOException {
		_document = null;
		_id = reader.readLong();
		reader.putObjectTable(_id, this);
		_pdfPage = reader.readObjectTable();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IRenderable renderable = reader.readObjectTable();
			_renderablesList.addLast(renderable);
			_renderablesMap.put(renderable.getId(), renderable);
		}
	}
	
	/**
	 * Serialize this page without its document.
	 * @param writer
	 * @throws IOException
	 */
	public void serializeDirect(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_pdfPage);
		writer.writeInt(_renderablesMap.size());
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			writer.writeObjectTable(r.getData());
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_document);
		writer.writeObjectTable(_pdfPage);
		writer.writeInt(_renderablesMap.size());
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			writer.writeObjectTable(r.getData());
		}
	}
	
	/**
	 * Clone the page for a new document
	 * @param dstDocument
	 * @return
	 */
	public DocumentPage clone(final IDocumentNotify dstDocument) {
		return new DocumentPage(dstDocument, this);
	}
}
