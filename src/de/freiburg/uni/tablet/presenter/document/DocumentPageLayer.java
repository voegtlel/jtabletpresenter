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

	public void addRenderable(final IRenderable renderable) {
		_renderablesMap.put(renderable.getId(), renderable);
		_renderablesList.addLast(renderable);
		_page.getParent().onObjectAdded(renderable);
		_page.getParent().fireRenderableAdded(renderable, this);
	}

	public void removeRenderable(final IRenderable renderable) {
		_renderablesMap.remove(renderable.getId());
		_renderablesList.remove(_renderablesList.getElement(renderable));
		_page.getParent().onObjectRemoved(renderable);
		_page.getParent().fireRenderableRemoved(renderable, this);
	}

	@Override
	public long getId() {
		return _id;
	}

	public DocumentPageLayer(final BinaryDeserializer reader,
			final Document document) throws IOException {
		_id = reader.readLong();
		final long pageId = reader.readLong();
		_page = (DocumentPage) document.getObject(pageId);
		final int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			final long id = reader.readLong();
			final IRenderable renderable = (IRenderable) document.getObject(id);
			_renderablesList.addLast(renderable);
			_renderablesMap.put(renderable.getId(), renderable);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeLong(_page.getId());
		writer.writeInt(_renderablesMap.size());
		for (LinkedElement<IRenderable> r = _renderablesList.getFirst(); r != null; r = r
				.getNext()) {
			writer.writeLong(r.getData().getId());
		}
	}
}
