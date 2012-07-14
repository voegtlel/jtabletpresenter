/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.HashMap;
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
public class Document implements IEntity {
	private final HashMap<Long, IEntity> _objects = new HashMap<Long, IEntity>();

	private final LinkedElementList<DocumentPage> _pages = new LinkedElementList<DocumentPage>();

	private final int _clientId;
	private int _uniqueId = 1;

	private final List<DocumentListener> _listeners = new LinkedList<DocumentListener>();

	/**
	 * 
	 */
	public Document(final int clientId) {
		_clientId = clientId;
		addPage();
	}

	/**
	 * Gets an object in the document
	 * 
	 * @param id
	 * @return
	 */
	public IEntity getObject(final long id) {
		return _objects.get(id);
	}

	/**
	 * Called, when an entity was added
	 * 
	 * @param entity
	 */
	public void onObjectAdded(final IEntity entity) {
		_objects.put(entity.getId(), entity);
	}

	/**
	 * Called, when an entity was removed
	 * 
	 * @param entity
	 */
	void onObjectRemoved(final IEntity entity) {
		_objects.remove(entity.getId());
	}

	/**
	 * Gets a page by its index
	 * 
	 * @param index
	 * @return
	 */
	public DocumentPage getPageByIndex(final int index,
			final boolean createIfNotExisting) {
		int i = index;
		for (LinkedElement<DocumentPage> j = _pages.getFirst(); j != null; j = j
				.getNext()) {
			if (i == 0) {
				return j.getData();
			}
			i--;
		}
		DocumentPage page = null;
		if (createIfNotExisting) {
			while (i > 0) {
				page = addPage();
			}
		}
		return page;
	}

	/**
	 * Gets a pages index
	 * 
	 * @param page
	 * @return
	 */
	public int getPageIndex(final DocumentPage page) {
		return _pages.getElementIndex(page);
	}

	/**
	 * Adds a document page
	 * 
	 * @return
	 */
	public DocumentPage addPage() {
		final DocumentPage result = new DocumentPage(this);
		final DocumentPage prevData = _pages.getLast() != null ? _pages
				.getLast().getData() : null;
		onObjectAdded(result);
		_pages.addLast(result);
		firePageInserted(prevData, result);
		return result;
	}

	/**
	 * Inserts a page after the given page.
	 * 
	 * @param afterPage
	 *            the previous page to the inserted page (null for first)
	 * @return
	 */
	public DocumentPage insertPage(final DocumentPage afterPage) {
		final DocumentPage result = new DocumentPage(this);
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, result);
			firePageInserted(element.getData(), result);
		} else {
			_pages.addFirst(result);
			firePageInserted(null, result);
		}
		return result;
	}

	/**
	 * @return
	 */
	public long getNextId() {
		return (long) _clientId << 32 | _uniqueId++;
	}

	@Override
	public long getId() {
		return _clientId << 32;
	}

	@Override
	public IEntity getParent() {
		return null;
	}

	void firePageInserted(final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageInserted(prevPage, page);
		}
	}

	void firePageRemoved(final DocumentPage prevPage, final DocumentPage page) {
		for (final DocumentListener listener : _listeners) {
			listener.pageRemoved(prevPage, page);
		}
	}

	void fireRenderableAdded(final IRenderable renderable,
			final DocumentPageLayer layer) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableAdded(renderable, layer);
		}
	}

	void fireRenderableRemoved(final IRenderable renderable,
			final DocumentPageLayer layer) {
		for (final DocumentListener listener : _listeners) {
			listener.renderableRemoved(renderable, layer);
		}
	}

	public void addListener(final DocumentListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(final DocumentListener listener) {
		_listeners.remove(listener);
	}

	public Document(final BinaryDeserializer reader) throws IOException {
		_uniqueId = reader.readInt();
		_clientId = reader.readInt();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IEntity entity = reader.readSerializableClass(new Class<?>[] {
					BinaryDeserializer.class, Document.class }, new Object[] {
					reader, this });
			_objects.put(entity.getId(), entity);
		}
		final int pageCount = _pages.getFirst().getNextCount();
		for (int i = 0; i < pageCount; i++) {
			final long pageId = reader.readLong();
			final DocumentPage page = (DocumentPage) getObject(pageId);
			_pages.addLast(page);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_uniqueId);
		writer.writeInt(_clientId);
		writer.writeInt(_objects.size());
		for (final IEntity entity : _objects.values()) {
			writer.writeSerializableClass(entity);
		}
		writer.writeInt(_pages.getFirst().getNextCount());
		for (LinkedElement<DocumentPage> dp = _pages.getFirst(); dp != null; dp = dp
				.getNext()) {
			writer.writeLong(dp.getData().getId());
		}
	}
}
