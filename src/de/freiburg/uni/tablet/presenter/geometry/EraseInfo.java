package de.freiburg.uni.tablet.presenter.geometry;

import java.util.HashMap;
import java.util.Map.Entry;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;

public class EraseInfo {
	private final HashMap<Long, IRenderable> _modifiedObjects = new HashMap<Long, IRenderable>();
	private final Document _document;
	private CollisionInfo _collisionInfo;
	private final DocumentPageLayer _layer;

	public EraseInfo(final Document document, final DocumentPageLayer layer) {
		_document = document;
		_layer = layer;
	}

	/**
	 * Creates the collision info
	 * 
	 * @param x
	 * @param y
	 * @param xOrig
	 * @param yOrig
	 * @param radiusX
	 * @param radiusY
	 * @param radiusXOrig
	 * @param radiusYOrig
	 * @param checkOnlyBoundaries
	 */
	public void createCollisionInfo(final float x, final float y,
			final float xOrig, final float yOrig, final float radiusX,
			final float radiusY, final float radiusXOrig,
			final float radiusYOrig, final boolean checkOnlyBoundaries) {
		_collisionInfo = new CollisionInfo(x, y, xOrig, yOrig, radiusX,
				radiusY, radiusXOrig, radiusYOrig, checkOnlyBoundaries);
	}

	/**
	 * Gets the assigned collision info
	 * 
	 * @return
	 */
	public CollisionInfo getCollisionInfo() {
		return _collisionInfo;
	}

	/**
	 * Adds a to be modified object to the list
	 * 
	 * @param origObject
	 *            original object
	 * @return copy of original to be modified
	 */
	public IRenderable addModifiedObject(final IRenderable origObject) {
		final IRenderable result = origObject.cloneRenderable(_document
				.getNextId());
		_modifiedObjects.put(origObject.getId(), result);
		return result;
	}

	/**
	 * Gets the copy of the original to be modified
	 * 
	 * @param origObject
	 *            original object
	 * @return copy of original to be modified
	 */
	public IRenderable getModifiedObject(final IRenderable origObject) {
		return _modifiedObjects.get(origObject.getId());
	}

	/**
	 * Applies all replace actions.
	 */
	public void applyModifications() {
		for (final Entry<Long, IRenderable> entry : _modifiedObjects.entrySet()) {
			final IRenderable renderable = (IRenderable) _document
					.getObject(entry.getKey());
			_layer.removeRenderable(renderable);
			_layer.addRenderable(entry.getValue());
		}
	}
}
