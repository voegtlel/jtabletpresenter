/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class Document implements IDocumentNotify {
	protected final LinkedElementList<DocumentPage> _pages = new LinkedElementList<DocumentPage>();

	private final UUID _docUuid;
	
	private int _docId;
	private int _uniqueId = 1;

	private final List<DocumentListener> _listeners = new LinkedList<DocumentListener>();

	/**
	 * Creates a new document by its docId
	 */
	protected Document(final int docId) {
		_docId = docId;
		_docUuid = UUID.randomUUID();
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected Document(final int docId, final Document base) {
		_docId = docId;
		_docUuid = UUID.randomUUID();
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
	public DocumentPage getPageById(final long id) {
		for (LinkedElement<DocumentPage> element = _pages.getFirst(); element != null; element = element.getNext()) {
			if (element.getData().getId() == id) {
				return element.getData();
			}
		}
		return null;
	}
	
	@Override
	public DocumentPage getNextPage(final DocumentPage page) {
		final LinkedElement<DocumentPage> pageElement = _pages.getElement(page);
		if (pageElement == null) {
			throw new IllegalArgumentException("Page not in document");
		}
		if (pageElement.getNext() == null) {
			return null;
		}
		return pageElement.getNext().getData();
	}
	
	@Override
	public DocumentPage getPreviousPage(final DocumentPage page) {
		final LinkedElement<DocumentPage> pageElement = _pages.getElement(page);
		if (pageElement == null) {
			throw new IllegalArgumentException("Page not in document");
		}
		if (pageElement.getPrevious() == null) {
			return null;
		}
		return pageElement.getPrevious().getData();
	}
	
	@Override
	public long nextId() {
		return (long) _docId << 32 | _uniqueId++;
	}
	
	@Override
	public void deserializeId(final long id) {
		int uniqueId = (int)(id & 0xffffffffl);
		if (uniqueId >= _uniqueId) {
			System.out.println("Deserializer increase uniqueId from " + _uniqueId + " to " + (uniqueId + 1));
			_uniqueId = uniqueId + 1;
		}
	}

	@Override
	public long getId() {
		return (long)_docId << 32;
	}
	
	@Override
	public UUID getUuid() {
		return _docUuid;
	}

	@Override
	public IEntity getParent() {
		return null;
	}
	
	@Override
	public Iterable<DocumentPage> getPages() {
		return _pages;
	}
	
	protected void firePageInserted(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageInserted(document, prevPage, page);
		}
	}

	protected void firePageRemoved(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
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
	public void fireRenderableAdding(final IRenderable afterRenderable, final IRenderable renderable,
			final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdding(afterRenderable, renderable, page);
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
	public void fireRenderableRemoving(final IRenderable afterRenderable,
			final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoving(afterRenderable, renderable, page);
		}
	}
	
	@Override
	public void fireRenderableModified(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModified(renderable, page);
		}
	}
	
	@Override
	public void fireRenderableModifying(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModifying(renderable, page);
		}
	}
	
	@Override
	public void fireRenderableModifyEnd(final IRenderable renderable, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableModifyEnd(renderable, page);
		}
	}
	
	@Override
	public void fireBackgroundEntityChanged(final DocumentPage documentPage,
			final IEntity lastBackgroundEntity) {
		for (final DocumentListener listener : _listeners) {
			listener.backgroundEntityChanged(documentPage, lastBackgroundEntity);
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
		final long uuidMsb = reader.readLong();
		final long uuidLsb = reader.readLong();
		_docUuid = new UUID(uuidMsb, uuidLsb);
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
		writer.writeLong(_docUuid.getMostSignificantBits());
		writer.writeLong(_docUuid.getLeastSignificantBits());
		// Always has first
		writer.writeInt(_pages.getCount());
		for (LinkedElement<DocumentPage> dp = _pages.getFirst(); dp != null; dp = dp
				.getNext()) {
			writer.writeObjectTable(dp.getData());
		}
	}
	
	@Override
	public String toString() {
		return String.format("Document %X (%X)", getId(), System.identityHashCode(this));
	}
}
