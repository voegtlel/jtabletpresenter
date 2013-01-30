/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class Document implements IDocumentNotify {
	protected final LinkedElementList<DocumentPage> _pages = new LinkedElementList<DocumentPage>();

	private int _docId;
	private int _uniqueId = 1;

	private final List<DocumentListener> _listeners = new LinkedList<DocumentListener>();

	/**
	 * Creates a new document by its docId
	 */
	protected Document(final int docId) {
		_docId = docId;
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected Document(final int docId, final Document base) {
		_docId = docId;
		for (LinkedElement<DocumentPage> j = _pages.getFirst(); j != null; j = j.getNext()) {
			_pages.addLast(j.getData().clone(this));
		}
	}
	
	@Override
	public DocumentPage getPageByIndex(final int index) {
		final LinkedElement<DocumentPage> element = _pages.getElementByIndex(index);
		if (element == null) {
			throw new IndexOutOfBoundsException("Page index out of range");
		}
		return element.getData();
	}
	
	@Override
	public int getPageCount() {
		return _pages.getCount();
	}
	
	@Override
	public boolean hasPage(final DocumentPage page) {
		final int result = _pages.getElementIndex(page);
		return result != -1;
	}
	
	@Override
	public int getPageIndex(final DocumentPage page) {
		final int result = _pages.getElementIndex(page);
		if (result == -1) {
			throw new IllegalArgumentException("Page not in document");
		}
		return result;
	}
	
	@Override
	public long nextId() {
		return (long) _docId << 32 | _uniqueId++;
	}

	@Override
	public long getId() {
		return (long)_docId << 32;
	}

	@Override
	public IEntity getParent() {
		return null;
	}

	protected void firePageInserted(final IEditableDocument document, final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageInserted(document, prevPage, page);
		}
	}

	protected void firePageRemoved(final IEditableDocument document, final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageRemoved(document, prevPage, page);
		}
	}

	@Override
	public void fireRenderableAdded(final IRenderable afterRenderable, final IRenderable renderable,
			final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdded(afterRenderable, renderable, page);
		}
	}
	
	@Override
	public void fireRenderableRemoved(final IRenderable afterRenderable,
			final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoved(afterRenderable, renderable, page);
		}
	}
	
	@Override
	public void fireRenderableModified(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModified(renderable, page);
		}
	}
	
	@Override
	public void fireRenderableModifyEnd(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModifyEnd(renderable, page);
		}
	}
	
	@Override
	public void firePdfPageChanged(final DocumentPage documentPage,
			final PdfPageSerializable lastPdfPage) {
		for (final DocumentListener listener : _listeners) {
			listener.pdfPageChanged(documentPage, lastPdfPage);
		}
	}

	@Override
	public void addListener(final DocumentListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(final DocumentListener listener) {
		_listeners.remove(listener);
	}

	protected Document(final BinaryDeserializer reader) throws IOException {
		// Read Ids
		_uniqueId = reader.readInt();
		_docId = reader.readInt();
		// Put this to object table
		reader.putObjectTable(this.getId(), this);
		// Read pages
		final int pageCount = reader.readInt();
		for (int i = 0; i < pageCount; i++) {
			final DocumentPage page = reader.readObjectTable();
			_pages.addLast(page);
		}
	}
	
	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_uniqueId);
		writer.writeInt(_docId);
		// Always has first
		writer.writeInt(_pages.getCount());
		for (LinkedElement<DocumentPage> dp = _pages.getFirst(); dp != null; dp = dp
				.getNext()) {
			writer.writeObjectTable(dp.getData());
		}
	}
}
