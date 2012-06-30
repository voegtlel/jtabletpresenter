/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

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
		_id = _page.getDocument().getNextId();
	}
	
	public void 

	public void addRenderable(final IRenderable renderable) {
		_renderablesMap.put(renderable.getId(), renderable);
		_renderablesList.addLast(renderable);
		_page.getDocument().onObjectAdded(renderable);
	}

	public void removeRenderable(final IRenderable renderable) {
		_renderablesMap.remove(renderable.getId());
		_renderablesList.remove(_renderablesList.getElement(renderable));
		_page.getDocument().onObjectRemoved(renderable);
	}

	@Override
	public long getId() {
		return _id;
	}
}
