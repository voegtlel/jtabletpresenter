/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.geometry.EraseInfo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

/**
 * @author lukas
 * 
 */
public class DocumentPageLayer implements IEntity {
	private final long _id;
	private final DocumentPage _page;

	private final LinkedElementList<IRenderable> _renderablesList = new LinkedElementList<IRenderable>();
	private final HashMap<Long, IRenderable> _renderablesMap = new HashMap<Long, IRenderable>();

	/**
	 * 
	 */
	public DocumentPageLayer(final DocumentPage page) {
		_page = page;
		_id = _page.getParent().getNextId();
	}

	@Override
	public DocumentPage getParent() {
		return _page;
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
		_page.getParent().fireRenderableAdded(renderable, this);
	}

	public void removeRenderable(final IRenderable renderable) {
		_renderablesMap.remove(renderable.getId());
		_renderablesList.remove(_renderablesList.getElement(renderable));
		_page.getParent().fireRenderableRemoved(renderable, this);
	}
	
	public boolean isEmpty() {
		return _renderablesList.isEmpty();
	}

	@Override
	public long getId() {
		return _id;
	}

	public DocumentPageLayer(final BinaryDeserializer reader)
			throws IOException {
		_id = reader.readLong();
		reader.putObjectTable(this.getId(), this);
		_page = reader.readObjectTable();
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final IRenderable renderable = reader.readObjectTable();
			_renderablesList.addLast(renderable);
			_renderablesMap.put(renderable.getId(), renderable);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_page.getId(), _page);
		writer.writeInt(_renderablesMap.size());
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			writer.writeObjectTable(r.getData().getId(), r.getData());
		}
	}
}
