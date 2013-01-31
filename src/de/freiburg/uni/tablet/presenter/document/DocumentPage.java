/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.geometry.CollisionInfo;
import de.freiburg.uni.tablet.presenter.geometry.CollisionListener;
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
			final IRenderable newRenderable = r.getData().cloneRenderable(this);
			_renderablesList.addLast(newRenderable);
		}
	}

	@Override
	public IDocument getParent() {
		return _document;
	}

	@Override
	public void render(final IPageBackRenderer renderer) {
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			r.getData().render(renderer);
		}
	}

	/**
	 * Collides the given info with the renderables and calls listener for each renderable hit
	 * @param collisionInfo
	 * @param listener
	 */
	public void collideWith(final CollisionInfo collisionInfo, final CollisionListener listener) {
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; ) {
			final LinkedElement<IRenderable> next = r.getNext();
			if (r.getData().collides(collisionInfo)) {
				listener.collides(r.getData());
			}
			r = next;
		}
	}
	
	/**
	 * Adds a renderable
	 * @param renderable
	 */
	public void addRenderable(final IRenderable renderable) {
		insertRenderable((_renderablesList.isEmpty()?null:_renderablesList.getLast().getData()), renderable);
	}

	/**
	 * Inserts a renderable
	 * @param afterRenderable
	 * @param renderable
	 */
	public void insertRenderable(final IRenderable afterRenderable, final IRenderable renderable) {
		if (afterRenderable != null) {
			final LinkedElement<IRenderable> element = _renderablesList.getElementByInstance(afterRenderable);
			_renderablesList.insertAfter(element, renderable);
		} else {
			_renderablesList.addFirst(renderable);
		}
		_document.fireRenderableAdded(afterRenderable, renderable, this);
	}

	/**
	 * Removes a renderable and returns the previous element
	 * @param renderable
	 * @return
	 */
	public IRenderable removeRenderable(final IRenderable renderable) {
		final LinkedElement<IRenderable> element = _renderablesList.getElementByInstance(renderable);
		IRenderable previous = null;
		if (element.getPrevious() != null) {
			previous = element.getPrevious().getData();
		}
		_renderablesList.remove(element);
		_document.fireRenderableRemoved(previous, renderable, this);
		return previous;
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
	 * Replaces a renderable
	 * @param renderable
	 * @param newRenderable
	 */
	public void replaceRenderable(final IRenderable renderable, final IRenderable newRenderable) {
		IRenderable previous = removeRenderable(renderable);
		insertRenderable(previous, newRenderable);
	}
	
	/**
	 * Clears the page
	 */
	public void clear() {
		while (!_renderablesList.isEmpty()) {
			final IRenderable removeElement = _renderablesList.getFirst().getData();
			_renderablesList.removeFirst();
			_document.fireRenderableRemoved(null, removeElement, this);
		}
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
	
	/**
	 * Called by renderable, when modified
	 * @param renderable
	 */
	public void fireRenderableModified(final IRenderable renderable) {
		_document.fireRenderableModified(renderable, this);
	}
	
	/**
	 * Called by renderable, when modifying ends
	 * @param renderable
	 */
	public void fireRenderableModifyEnd(final IRenderable renderable) {
		_document.fireRenderableModifyEnd(renderable, this);
	}

	public DocumentPage(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
		reader.putObjectTable(this.getId(), this);
		_document = reader.readObjectTable();
		_document.deserializeId(_id);
		_pdfPage = reader.readObjectTable();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IRenderable renderable = reader.readObjectTable();
			_renderablesList.addLast(renderable);
		}
	}
	
	/**
	 * Overwrites the contents of this page with a previously direct serialized page
	 * @param reader
	 * @throws IOException
	 */
	public void deserializeDirect(final BinaryDeserializer reader) throws IOException {
		clear();
		reader.putObjectTable(_id, this);
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IRenderable renderable = reader.readObjectTable();
			LinkedElement<IRenderable> prevRenderable = _renderablesList.getLast();
			_renderablesList.addLast(renderable);
			_document.fireRenderableAdded((prevRenderable == null?null:prevRenderable.getData()), renderable, this);
		}
	}
	
	/**
	 * Serialize this page without its document.
	 * @param writer
	 * @throws IOException
	 */
	public void serializeDirect(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeInt(_renderablesList.getCount());
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
		writer.writeInt(_renderablesList.getCount());
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
